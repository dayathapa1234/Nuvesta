package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "yahoo.update.enabled", havingValue = "true", matchIfMissing = true)
public class YahooPriceSyncScheduler {
    private final JobLauncher jobLauncher;
    private final Job importYahooPriceJob;
    private final DailyPriceRepository dailyPriceRepository;
    private final SymbolInfoRepository symbolInfoRepository;
    private final List<String> configuredSymbols;

    public YahooPriceSyncScheduler(JobLauncher jobLauncher,
                                   Job importYahooPriceJob,
                                   DailyPriceRepository dailyPriceRepository,
                                   SymbolInfoRepository symbolInfoRepository,
                                   @Value("${yahoo.symbols:}") String yahooSymbols) {
        this.jobLauncher = jobLauncher;
        this.importYahooPriceJob = importYahooPriceJob;
        this.dailyPriceRepository = dailyPriceRepository;
        this.symbolInfoRepository = symbolInfoRepository;
        this.configuredSymbols = parseSymbols(yahooSymbols);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() throws Exception {
        runJobIfMissing();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void syncDaily() throws Exception {
        runJobIfMissing();
    }

    private void runJobIfMissing() throws Exception {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        DayOfWeek dow = todayUtc.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return;
        }

        List<String> symbols = configuredSymbols.isEmpty()
                ? symbolInfoRepository.findAll().stream().map(SymbolInfo::getSymbol).toList()
                : symbolInfoRepository.findAllById(configuredSymbols).stream().map(SymbolInfo::getSymbol).toList();

        boolean missing = symbols.stream()
                .anyMatch(sym -> !dailyPriceRepository.existsBySymbolAndDate(sym, todayUtc));

        if (!missing) {
            return;
        }

        JobParameters params = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importYahooPriceJob, params);
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