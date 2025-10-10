package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.events.SymbolCatalogLoadedEvent;
import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.MarketDataService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("!test")
public class SymbolInfoSyncScheduler {

    private static final long MIN_SYMBOL_BOOTSTRAP_COUNT = 100;

    private final SymbolInfoRepository repository;
    private final MarketDataService marketDataService;
    private final JobLauncher jobLauncher;
    private final Job importSymbolJob;
    private final ApplicationEventPublisher eventPublisher;

    public SymbolInfoSyncScheduler(SymbolInfoRepository repository,
                                   MarketDataService marketDataService,
                                   JobLauncher jobLauncher,
                                   Job importSymbolJob,
                                   ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.marketDataService = marketDataService;
        this.jobLauncher = jobLauncher;
        this.importSymbolJob = importSymbolJob;
        this.eventPublisher = eventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() throws Exception {
        long existingSymbols = repository.count();
        if (existingSymbols < MIN_SYMBOL_BOOTSTRAP_COUNT){
            JobParameters params = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(importSymbolJob, params);
            existingSymbols = repository.count();
        }

        if (existingSymbols > 0) {
            eventPublisher.publishEvent(new SymbolCatalogLoadedEvent(this));
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void syncDaily() {
        List<SymbolInfo> allSymbols = marketDataService.getAllSymbols();
        Set<String> existingSymbols = repository.findAll().stream()
                .map(SymbolInfo::getSymbol).collect(Collectors.toSet());

        List<SymbolInfo> newSymbols = allSymbols.stream()
                .filter(info -> !existingSymbols.contains(info.getSymbol()))
                .toList();

        if (!newSymbols.isEmpty()) {
            repository.saveAll(newSymbols);
            eventPublisher.publishEvent(new SymbolCatalogLoadedEvent(this));
        }
    }
}
