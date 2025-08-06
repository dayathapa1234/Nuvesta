package com.nuvesta.market_data_service.service;

import com.nuvesta.market_data_service.model.SymbolInfo;

import java.util.List;

public interface MarketDataService {
    List<SymbolInfo> getAllSymbols();
}
