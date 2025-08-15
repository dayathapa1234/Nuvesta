package com.nuvesta.market_data_service.batch;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.impl.StooqService;
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

    private final SymbolInfoRepository symbolInfoRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final StooqService stooqService;

    public StooqPriceSyncScheduler(SymbolInfoRepository symbolInfoRepository,
                                   DailyPriceRepository dailyPriceRepository,
                                   StooqService stooqService) {
        this.symbolInfoRepository = symbolInfoRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.stooqService = stooqService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() {
        updatePrices();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void syncDaily() {
        updatePrices();
    }

    private void updatePrices() {
        symbolInfoRepository.findAll().forEach(info -> {
            LocalDate lastDate = dailyPriceRepository.findTopBySymbolOrderByDateDesc(info.getSymbol())
                    .map(DailyPrice::getDate)
                    .orElse(null);
            var prices = stooqService.fetchPricesAfter(info.getSymbol(), lastDate);
            if (!prices.isEmpty()) {
                dailyPriceRepository.saveAll(prices);
            }
        });
    }
}