package com.nuvesta.market_data_service.service;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public Page<SymbolInfo> getSymbols(String name, String symbol, String assetType, String delistingDate, String exchange, String ipoDate, String status, Pageable pageable) {
        List<Specification<SymbolInfo>> parts = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            parts.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (symbol != null && !symbol.isBlank()) {
            parts.add((root, query, cb) ->
                    cb.equal(cb.lower(root.get("symbol")), symbol.toLowerCase()));
        }
        if (assetType != null && !assetType.isBlank()) {
            parts.add((root, query, cb) ->
                    cb.equal(cb.lower(root.get("assetType")), assetType.toLowerCase()));
        }
        if (delistingDate != null && !delistingDate.isBlank()) {
            parts.add((root, query, cb) -> cb.equal(root.get("delistingDate"), delistingDate));
        }
        if (exchange != null && !exchange.isBlank()) {
            parts.add((root, query, cb) ->
                    cb.equal(cb.lower(root.get("exchange")), exchange.toLowerCase()));
        }
        if (ipoDate != null && !ipoDate.isBlank()) {
            parts.add((root, query, cb) -> cb.equal(root.get("ipoDate"), ipoDate));
        }
        if (status != null && !status.isBlank()) {
            parts.add((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
        }

        Specification<SymbolInfo> spec = Specification.allOf(parts);
        return symbolInfoRepository.findAll(spec, pageable);
    }
}
