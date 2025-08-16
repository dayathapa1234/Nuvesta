package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.SymbolInfo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
@Profile("!test")
@EnableBatchProcessing
public class AlphaVantageSymbolImportJobConfig {
    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Bean
    public Job importSymbolJob(JobRepository jobRepository, Step importStep){
        return new JobBuilder("importSymbolJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importStep)
                .build();
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<SymbolInfo> reader,
                           ItemWriter<SymbolInfo> writer){
        return new StepBuilder("importStep", jobRepository)
                .<SymbolInfo,SymbolInfo>chunk(100,transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<SymbolInfo> symbolInfoReader() {
        String url = "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=" + apiKey;
        String csv = restTemplate.getForObject(url, String.class);

        List<SymbolInfo> symbols = new ArrayList<>();

        if (csv != null && !csv.isBlank()) {
            try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
                String line;
                boolean isFirst = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 7) {
                        symbols.add(new SymbolInfo(
                                parts[0], // symbol
                                parts[1], // name
                                parts[2], // exchange
                                parts[3], // assetType
                                parts[4], // ipoDate
                                parts[5], // delistingDate
                                parts[6],  // status
                                null      // latestPrice
                        ));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Alpha Vantage CSV", e);
            }
        }

        List<SymbolInfo> nonNullSymbols = symbols.stream()
                .filter(Objects::nonNull)
                .toList();

        return new ItemReader<>() {
            private int nextIndex = 0;

            @Override
            public SymbolInfo read() {
                if (nextIndex < nonNullSymbols.size()) {
                    return nonNullSymbols.get(nextIndex++);
                }
                return null;
            }
        };
    }


    @Bean
    public ItemWriter<SymbolInfo> symbolInfoWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<SymbolInfo>()
                .dataSource(dataSource)
                .sql("INSERT INTO symbol_info (symbol, name, exchange, asset_type, ipo_date, delisting_date, status) " +
                        "VALUES (:symbol, :name, :exchange, :assetType, :ipoDate, :delistingDate, :status)")
                .beanMapped()
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
