package com.nuvesta.market_data_service.service;

import com.nuvesta.market_data_service.model.SymbolInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MarketDataService {
    List<SymbolInfo> getAllSymbols();

    List<String> getDistinctExchanges();

    List<String> getDistinctAssetTypes();

    List<String> getDistinctIpoDates();

    Page<SymbolInfo> getSymbols(String keyword,
                                List<String> assetTypes,
                                String delistingDate,
                                List<String> exchanges,
                                List<String> ipoDates,
                                String status,
                                Pageable pageable);
}
