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

    @GetMapping("/api/paginatedSymbols")
    public ResponseEntity<List<SymbolInfo>> getPaginatedSymbols(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String symbol,
            @RequestParam(name = "asset_type", required = false) String assetType,
            @RequestParam(name = "delisting_date", required = false) String delistingDate,
            @RequestParam(required = false) String exchange,
            @RequestParam(name = "ipo_date", required = false) String ipoDate,
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
                name, symbol, assetType, delistingDate, exchange, ipoDate, status, pageable
        );

        return ResponseEntity.ok()
                .header("X-Total-Elements", String.valueOf(result.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }
}
