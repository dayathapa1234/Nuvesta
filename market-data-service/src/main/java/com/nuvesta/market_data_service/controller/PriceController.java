package com.nuvesta.market_data_service.controller;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.service.PriceService;
import com.nuvesta.market_data_service.service.impl.YahooOnDemandSyncService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping
public class PriceController {

    private final PriceService priceService;
    private final YahooOnDemandSyncService onDemandSyncService;

    public PriceController(PriceService priceService, YahooOnDemandSyncService onDemandSyncService) {
        this.priceService = priceService;
        this.onDemandSyncService = onDemandSyncService;
    }

    public record PricePoint(long time, BigDecimal price) { }

    @GetMapping("/api/prices")
    public List<PricePoint> getPrices(@RequestParam String symbol,
                                      @RequestParam(value = "from", required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                      @RequestParam(value = "autofetch", required = false, defaultValue = "true") boolean autofetch) {

        // Optionally fetch any missing data before returning
        if (autofetch) {
            onDemandSyncService.ensureUpToDate(symbol);
        }

        List<DailyPrice> prices = priceService.fetchPricesAfter(symbol, from);
        return prices.stream()
                .map(p -> new PricePoint(p.getDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(), p.getClose()))
                .toList();
    }
}
