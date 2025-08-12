package com.nuvesta.market_data_service.service;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class AlphaVantageService implements MarketDataService{

    private final SymbolInfoRepository symbolInfoRepository;

    public AlphaVantageService(SymbolInfoRepository symbolInfoRepository) {
        this.symbolInfoRepository = symbolInfoRepository;
    }

    @Override
    public List<SymbolInfo> getAllSymbols() {
        return symbolInfoRepository.findAll();
    }
}
