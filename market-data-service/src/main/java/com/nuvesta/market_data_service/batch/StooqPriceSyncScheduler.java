package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.impl.StooqService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "stooq.update.enabled", havingValue = "true", matchIfMissing = true)
public class StooqPriceSyncScheduler {

    private final JobLauncher jobLauncher;
    private final Job importStooqPriceJob;

    public StooqPriceSyncScheduler(JobLauncher jobLauncher, Job importStooqPriceJob) {
        this.jobLauncher = jobLauncher;
        this.importStooqPriceJob = importStooqPriceJob;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() throws Exception {
        runJob();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void syncDaily() throws Exception {
        runJob();
    }

    private void runJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importStooqPriceJob, params);
    }
}