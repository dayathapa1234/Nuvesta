package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.AlphaVantageService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SymbolInfoSyncScheduler {

    private final SymbolInfoRepository repository;
    private final AlphaVantageService alphaVantageService;
    private final JobLauncher jobLauncher;
    private final Job importSymbolJob;


    public SymbolInfoSyncScheduler(SymbolInfoRepository repository, AlphaVantageService alphaVantageService, JobLauncher jobLauncher, Job importSymbolJob) {
        this.repository = repository;
        this.alphaVantageService = alphaVantageService;
        this.jobLauncher = jobLauncher;
        this.importSymbolJob = importSymbolJob;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() throws Exception {
        if (repository.count() == 0){
            JobParameters params = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(importSymbolJob, params);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void syncDaily() {
        List<SymbolInfo> allSymbols = alphaVantageService.getAllSymbols();
        Set<String> existingSymbols = repository.findAll().stream()
                .map(SymbolInfo::getSymbol).collect(Collectors.toSet());

        List<SymbolInfo> newSymbols = allSymbols.stream()
                .filter(info -> !existingSymbols.contains(info.getSymbol()))
                .toList();

        if (!newSymbols.isEmpty()) {
            repository.saveAll(newSymbols);
        }
    }
}
