package com.nuvesta.market_data_service.service.impl;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.service.YahooFinanceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class YahooOnDemandSyncService {

    private static final Logger log = LoggerFactory.getLogger(YahooOnDemandSyncService.class);

    private final DailyPriceRepository dailyPriceRepository;
    private final YahooFinanceClient yahooFinanceClient;

    public YahooOnDemandSyncService(DailyPriceRepository dailyPriceRepository,
                                    YahooFinanceClient yahooFinanceClient) {
        this.dailyPriceRepository = dailyPriceRepository;
        this.yahooFinanceClient = yahooFinanceClient;
    }

    public void ensureUpToDate(String symbol) {
        if (symbol == null || symbol.isBlank()) return;

        final LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        final LocalDate endEff = lastWeekday(todayUtc);

        LocalDate lastDate = dailyPriceRepository.findTopBySymbolOrderByDateDesc(symbol)
                .map(DailyPrice::getDate)
                .orElse(null);

        LocalDate start = (lastDate == null) ? LocalDate.of(1990, 1, 1) : lastDate.plusDays(1);
        if (start.isAfter(endEff)) return; // up to date

        log.info("On-demand Yahoo fetch {}: {} → {}", symbol, start, endEff);
        List<DailyPrice> fetched = yahooFinanceClient.fetchHistory(symbol, start, endEff);
        if (fetched.isEmpty()) return;

        List<DailyPrice> newRows = new ArrayList<>(fetched.size());
        for (DailyPrice p : fetched) {
            if (lastDate == null || p.getDate().isAfter(lastDate)) {
                newRows.add(p);
            }
        }
        if (!newRows.isEmpty()) {
            dailyPriceRepository.saveAll(newRows);
            LocalDate min = newRows.stream().map(DailyPrice::getDate).min(LocalDate::compareTo).orElse(null);
            LocalDate max = newRows.stream().map(DailyPrice::getDate).max(LocalDate::compareTo).orElse(null);
            log.info("Inserted {} rows for {} ({} → {})", newRows.size(), symbol, min, max);
        }
    }

    private static LocalDate lastWeekday(LocalDate d) {
        DayOfWeek dow = d.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return d.minusDays(1);
        if (dow == DayOfWeek.SUNDAY)   return d.minusDays(2);
        return d;
    }
}

