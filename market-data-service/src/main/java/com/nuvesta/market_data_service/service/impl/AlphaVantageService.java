package com.nuvesta.market_data_service.service.impl;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import com.nuvesta.market_data_service.service.MarketDataService;
import com.nuvesta.market_data_service.service.PriceService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Primary
public class AlphaVantageService implements MarketDataService {

    private final SymbolInfoRepository symbolInfoRepository;
    private final DailyPriceRepository dailyPriceRepository;

    public AlphaVantageService(SymbolInfoRepository symbolInfoRepository, DailyPriceRepository dailyPriceRepository) {
        this.symbolInfoRepository = symbolInfoRepository;
        this.dailyPriceRepository = dailyPriceRepository;}

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
        Page<SymbolInfo> pageResult = symbolInfoRepository.findAll(spec, pageable);

        List<String> symbols = pageResult.getContent().stream()
                .map(SymbolInfo::getSymbol)
                .toList();

        if (!symbols.isEmpty()) {
            Map<String, BigDecimal> priceMap = dailyPriceRepository
                    .findLatestCloseBySymbolIn(symbols)
                    .stream()
                    .collect(Collectors.toMap(r -> (String) r[0], r -> (BigDecimal) r[1]));

            pageResult.getContent().forEach(si ->
                    si.setLatestPrice(priceMap.get(si.getSymbol())));
        }

        return pageResult;
    }
}