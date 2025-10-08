package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.YahooFinanceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Profile("!test")
@EnableBatchProcessing
public class YahooPriceImporterJobConfig {

    private static final Logger log = LoggerFactory.getLogger(YahooPriceImporterJobConfig.class);

    private final SymbolInfoRepository symbolInfoRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final YahooFinanceClient yahooFinanceClient;
    private final long requestDelayMs;
    private final List<String> configuredSymbols;

    public YahooPriceImporterJobConfig(SymbolInfoRepository symbolInfoRepository,
                                       DailyPriceRepository dailyPriceRepository,
                                       YahooFinanceClient yahooFinanceClient,
                                       @Value("${yahoo.request.delay-ms:800}") long requestDelayMs,
                                       @Value("${yahoo.symbols:}") String yahooSymbols) {
        this.symbolInfoRepository = symbolInfoRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.yahooFinanceClient = yahooFinanceClient;
        this.requestDelayMs = requestDelayMs;
        this.configuredSymbols = parseSymbols(yahooSymbols);
    }

    @Bean
    public Job importYahooPriceJob(JobRepository jobRepository, Step yahooPriceImportStep) {
        return new JobBuilder("importYahooPriceJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(yahooPriceImportStep)
                .build();
    }

    @Bean
    public Step yahooPriceImportStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     @Qualifier("yahooPriceReader") ItemReader<DailyPrice> yahooPriceReader,
                                     @Qualifier("yahooPriceWriter") ItemWriter<DailyPrice> yahooPriceWriter) {
        return new StepBuilder("yahooPriceImportStep", jobRepository)
                .<DailyPrice, DailyPrice>chunk(1000, transactionManager)
                .reader(yahooPriceReader)
                .writer(yahooPriceWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<DailyPrice> yahooPriceReader() {
        final LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        final LocalDate endEff = lastWeekday(todayUtc);

        List<SymbolInfo> symbolsToProcess = configuredSymbols.isEmpty()
                ? symbolInfoRepository.findAll()
                : symbolInfoRepository.findAllById(configuredSymbols);

        Iterator<SymbolInfo> symbolIterator = symbolsToProcess.iterator();

        return new ItemReader<>() {
            private Iterator<DailyPrice> currentPrices = Collections.emptyIterator();

            @Override
            public synchronized DailyPrice read() {
                while (!currentPrices.hasNext()) {
                    if (!symbolIterator.hasNext()) {
                        return null;
                    }

                    SymbolInfo info = symbolIterator.next();
                    String symbol = info.getSymbol();

                    LocalDate lastDate = dailyPriceRepository.findTopBySymbolOrderByDateDesc(symbol)
                            .map(DailyPrice::getDate)
                            .orElse(null);

                    LocalDate startDate = (lastDate == null) ? LocalDate.of(1990, 1, 1) : lastDate.plusDays(1);

                    if (startDate.isAfter(endEff)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Skip {}: start {} > end {}", symbol, startDate, endEff);
                        }
                        continue;
                    }

                    log.info("Yahoo fetch window {}: {} -> {}", symbol, startDate, endEff);

                    List<DailyPrice> fetched = yahooFinanceClient.fetchHistory(symbol, startDate, endEff);

                    if (requestDelayMs > 0) {
                        try {
                            Thread.sleep(requestDelayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("Interrupted while throttling Yahoo requests", ie);
                        }
                    }

                    if (fetched.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("No rows returned for {}", symbol);
                        }
                        continue;
                    }

                    List<DailyPrice> filtered = fetched.stream()
                            .filter(p -> lastDate == null || p.getDate().isAfter(lastDate))
                            .collect(Collectors.toList());

                    if (filtered.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("No new rows for {}", symbol);
                        }
                        continue;
                    }

                    LocalDate min = filtered.stream().map(DailyPrice::getDate).min(LocalDate::compareTo).orElse(null);
                    LocalDate max = filtered.stream().map(DailyPrice::getDate).max(LocalDate::compareTo).orElse(null);
                    log.info("Queued {} rows for {} ({} -> {})", filtered.size(), symbol, min, max);

                    currentPrices = filtered.iterator();
                }

                return currentPrices.next();
            }
        };
    }

    @Bean
    public ItemWriter<DailyPrice> yahooPriceWriter(DataSource dataSource) {
        JdbcBatchItemWriter<DailyPrice> delegate = new JdbcBatchItemWriterBuilder<DailyPrice>()
                .dataSource(dataSource)
                .sql("INSERT INTO daily_price (symbol, date, open, high, low, close, volume) " +
                        "VALUES (:symbol, :date, :open, :high, :low, :close, :volume) " +
                        "ON CONFLICT (symbol, date) DO NOTHING")
                .beanMapped()
                // Skip assertion since ON CONFLICT DO NOTHING returns 0 for existing rows
                .assertUpdates(false)
                .build();

        delegate.afterPropertiesSet();

        return (items) -> { // items is a Chunk<? extends DailyPrice>
            if (!items.isEmpty()) {
                List<? extends DailyPrice> list = items.getItems();
                DailyPrice first = list.get(0);
                DailyPrice last  = list.get(list.size() - 1);
                log.info("Writing {} rows ({} {} → {} {})",
                        items.size(),
                        first.getSymbol(), first.getDate(),
                        last.getSymbol(), last.getDate());
            }
            // JdbcBatchItemWriter in Spring Batch 5 also expects a Chunk
            delegate.write(items);
        };
    }

    private static LocalDate lastWeekday(LocalDate d) {
        DayOfWeek dow = d.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return d.minusDays(1);
        if (dow == DayOfWeek.SUNDAY)   return d.minusDays(2);
        return d;
    }

    private static List<String> parseSymbols(String raw) {
        if (raw == null) return Collections.emptyList();
        String trim = raw.trim();
        if (trim.isEmpty() || trim.equalsIgnoreCase("full")) return Collections.emptyList();
        return Arrays.stream(trim.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
