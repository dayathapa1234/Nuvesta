package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
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
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.nuvesta.market_data_service.service.YahooFinanceClient;

@Configuration
@Profile("!test")
@EnableBatchProcessing
public class YahooPriceImporterJobConfig {

    private final SymbolInfoRepository symbolInfoRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final YahooFinanceClient yahooFinanceClient;
    private final long requestDelayMs;

    public YahooPriceImporterJobConfig(SymbolInfoRepository symbolInfoRepository,
                                       DailyPriceRepository dailyPriceRepository, YahooFinanceClient yahooFinanceClient,
                                       @Value("${yahoo.request.delay-ms:1200}") long requestDelayMs) {
        this.symbolInfoRepository = symbolInfoRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.yahooFinanceClient = yahooFinanceClient;
        this.requestDelayMs = requestDelayMs;
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
                .<DailyPrice, DailyPrice>chunk(100, transactionManager)
                .reader(yahooPriceReader)
                .writer(yahooPriceWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<DailyPrice> yahooPriceReader() {
        List<DailyPrice> prices = new ArrayList<>();
        symbolInfoRepository.findAll().forEach(info -> {
            LocalDate lastDate = dailyPriceRepository.findTopBySymbolOrderByDateDesc(info.getSymbol())
                    .map(DailyPrice::getDate)
                    .orElse(null);

            LocalDate start = lastDate != null ? lastDate.plusDays(1) : LocalDate.of(1990, 1, 1);
            LocalDate end = LocalDate.now();

            List<DailyPrice> fetched = yahooFinanceClient.fetchHistory(info.getSymbol(), start, end);
            for (DailyPrice price : fetched) {
                if (lastDate == null || price.getDate().isAfter(lastDate)) {
                    prices.add(price);
                }
            }

            try {
                Thread.sleep(requestDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return new ItemReader<>() {
            private int nextIndex = 0;

            @Override
            public DailyPrice read() {
                if (nextIndex < prices.size()) {
                    return prices.get(nextIndex++);
                }
                return null;
            }
        };
    }

    @Bean
    public ItemWriter<DailyPrice> yahooPriceWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<DailyPrice>()
                .dataSource(dataSource)
                .sql("INSERT INTO daily_price (symbol, date, open, high, low, close, volume) " +
                        "VALUES (:symbol, :date, :open, :high, :low, :close, :volume) " +
                        "ON CONFLICT (symbol, date) DO NOTHING")
                .beanMapped()
                .build();
    }
}