package com.nuvesta.market_data_service.service;

import com.nuvesta.market_data_service.model.SymbolInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MarketDataService {
    List<SymbolInfo> getAllSymbols();

    Page<SymbolInfo> getSymbols(String name, String symbol, String assetType, String delistingDate, String exchange, String ipoDate, String status, Pageable pageable);
}
