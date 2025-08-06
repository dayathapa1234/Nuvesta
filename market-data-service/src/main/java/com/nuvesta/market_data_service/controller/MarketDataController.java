package com.nuvesta.market_data_service.controller;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.service.MarketDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class MarketDataController {

    private final MarketDataService marketDataService;


    public MarketDataController(MarketDataService service) {
        this.marketDataService = service;
    }

    @GetMapping("/api/symbols")
    public List<SymbolInfo> getSymbols() {
        return marketDataService.getAllSymbols();
    }
}
