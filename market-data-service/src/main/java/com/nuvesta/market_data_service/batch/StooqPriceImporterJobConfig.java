package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.impl.StooqService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("!test")
@EnableBatchProcessing
public class StooqPriceImporterJobConfig {

    private final SymbolInfoRepository symbolInfoRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final RestTemplate restTemplate;

    public StooqPriceImporterJobConfig(SymbolInfoRepository symbolInfoRepository,
                                       DailyPriceRepository dailyPriceRepository,
                                       RestTemplateBuilder restTemplateBuilder) {
        this.symbolInfoRepository = symbolInfoRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Bean
    public Job importStooqPriceJob(JobRepository jobRepository, Step priceImportStep) {
        return new JobBuilder("importStooqPriceJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(priceImportStep)
                .build();
    }

    @Bean
    public Step priceImportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ItemReader<DailyPrice> reader,
                                ItemWriter<DailyPrice> writer) {
        return new StepBuilder("priceImportStep", jobRepository)
                .<DailyPrice, DailyPrice>chunk(100, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<DailyPrice> stooqPriceReader() {
        List<DailyPrice> prices = new ArrayList<>();
        symbolInfoRepository.findAll().forEach(info -> {
            LocalDate lastDate = dailyPriceRepository.findTopBySymbolOrderByDateDesc(info.getSymbol())
                    .map(DailyPrice::getDate)
                    .orElse(null);

            String url = "https://stooq.com/q/d/l/?s=" + info.getSymbol().toLowerCase() + ".us&i=d";
            String body = restTemplate.getForObject(url, String.class);
            if (body == null || body.isBlank()) {
                return;
            }
            Arrays.stream(body.trim().split("\n"))
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(tokens -> tokens.length >= 6)
                    .forEach(tokens -> {
                        LocalDate date = LocalDate.parse(tokens[0]);
                        if (lastDate == null || date.isAfter(lastDate)) {
                            DailyPrice price = new DailyPrice();
                            price.setSymbol(info.getSymbol().toUpperCase());
                            price.setDate(date);
                            price.setOpen(new BigDecimal(tokens[1]));
                            price.setHigh(new BigDecimal(tokens[2]));
                            price.setLow(new BigDecimal(tokens[3]));
                            price.setClose(new BigDecimal(tokens[4]));
                            try {
                                price.setVolume(Long.parseLong(tokens[5]));
                            } catch (NumberFormatException e) {
                                price.setVolume(0L);
                            }
                            prices.add(price);
                        }
                    });
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
    public ItemWriter<DailyPrice> stooqPriceWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<DailyPrice>()
                .dataSource(dataSource)
                .sql("INSERT INTO daily_price (symbol, date, open, high, low, close, volume) " +
                        "VALUES (:symbol, :date, :open, :high, :low, :close, :volume) " +
                        "ON CONFLICT (symbol, date) DO NOTHING")
                .beanMapped()
                .build();
    }
}
