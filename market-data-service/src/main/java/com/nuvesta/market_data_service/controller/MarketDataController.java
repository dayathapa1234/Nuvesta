package com.nuvesta.market_data_service.controller;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.service.MarketDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/api/symbol-filters")
    public Map<String, List<String>> getSymbolFilters() {
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("exchanges", marketDataService.getDistinctExchanges());
        filters.put("assetTypes", marketDataService.getDistinctAssetTypes());
        filters.put("ipoDates", marketDataService.getDistinctIpoDates());
        return filters;
    }

    @GetMapping("/api/paginatedSymbols")
    public ResponseEntity<List<SymbolInfo>> getPaginatedSymbols(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "asset_type", required = false) java.util.List<String> assetTypes,
            @RequestParam(name = "delisting_date", required = false) String delistingDate,
            @RequestParam(required = false) java.util.List<String> exchange,
            @RequestParam(name = "ipo_date", required = false) java.util.List<String> ipoDates,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "symbol") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SymbolInfo> result = marketDataService.getSymbols(
                keyword, assetTypes, delistingDate, exchange, ipoDates, status, pageable
        );

        return ResponseEntity.ok()
                .header("X-Total-Elements", String.valueOf(result.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }
}
