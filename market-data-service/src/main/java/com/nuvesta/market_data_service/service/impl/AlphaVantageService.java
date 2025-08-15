package com.nuvesta.market_data_service.service.impl;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.MarketDataService;
import com.nuvesta.market_data_service.service.PriceService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class AlphaVantageService implements MarketDataService {

    private final SymbolInfoRepository symbolInfoRepository;
    private final PriceService priceService;

    public AlphaVantageService(SymbolInfoRepository symbolInfoRepository, PriceService priceService) {
        this.symbolInfoRepository = symbolInfoRepository;
        this.priceService = priceService;
    }

    @Override
    public List<SymbolInfo> getAllSymbols() {
        return symbolInfoRepository.findAll();
    }

    @Override
    public List<String> getDistinctExchanges() {
        return symbolInfoRepository.findDistinctExchanges();
    }

    @Override
    public List<String> getDistinctAssetTypes() {
        return symbolInfoRepository.findDistinctAssetTypes();
    }

    @Override
    public List<String> getDistinctIpoDates() {
        return symbolInfoRepository.findDistinctIpoDates();
    }

    @Override
    public Page<SymbolInfo> getSymbols(String keyword,
                                       List<String> assetTypes,
                                       String delistingDate,
                                       List<String> exchanges,
                                       List<String> ipoDates,
                                       String status,
                                       Pageable pageable) {
        List<Specification<SymbolInfo>> parts = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            parts.add((root, query, cb) -> {
                String like = "%" + keyword.toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("symbol")), like)
                );
            });
        }
        if (assetTypes != null && !assetTypes.isEmpty()) {
            List<String> lower = assetTypes.stream().map(String::toLowerCase).toList();
            parts.add((root, query, cb) -> cb.lower(root.get("assetType")).in(lower));
        }
        if (delistingDate != null && !delistingDate.isBlank()) {
            parts.add((root, query, cb) -> cb.equal(root.get("delistingDate"), delistingDate));
        }
        if (exchanges != null && !exchanges.isEmpty()) {
            List<String> lower = exchanges.stream().map(String::toLowerCase).toList();
            parts.add((root, query, cb) -> cb.lower(root.get("exchange")).in(lower));
        }
        if (ipoDates != null && !ipoDates.isEmpty()) {
            parts.add((root, query, cb) -> root.get("ipoDate").in(ipoDates));
        }
        if (status != null && !status.isBlank()) {
            parts.add((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
        }

        Specification<SymbolInfo> spec = Specification.allOf(parts);
        return symbolInfoRepository.findAll(spec, pageable);
    }
}
