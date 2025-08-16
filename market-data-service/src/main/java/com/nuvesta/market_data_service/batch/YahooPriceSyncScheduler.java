package com.nuvesta.market_data_service.batch;

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

@Component
@Profile("!test")
@ConditionalOnProperty(name = "yahoo.update.enabled", havingValue = "true", matchIfMissing = true)
public class YahooPriceSyncScheduler {

    private final JobLauncher jobLauncher;
    private final Job importYahooPriceJob;

    public YahooPriceSyncScheduler(JobLauncher jobLauncher, Job importYahooPriceJob) {
        this.jobLauncher = jobLauncher;
        this.importYahooPriceJob = importYahooPriceJob;
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
        jobLauncher.run(importYahooPriceJob, params);
    }
}