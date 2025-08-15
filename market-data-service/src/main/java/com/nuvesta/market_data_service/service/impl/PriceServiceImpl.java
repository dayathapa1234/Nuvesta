package com.nuvesta.market_data_service.service.impl;

import com.nuvesta.market_data_service.model.DailyPrice;
import com.nuvesta.market_data_service.repository.DailyPriceRepository;
import com.nuvesta.market_data_service.service.PriceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PriceServiceImpl implements PriceService {

    private final DailyPriceRepository dailyPriceRepository;

    public PriceServiceImpl(DailyPriceRepository dailyPriceRepository) {
        this.dailyPriceRepository = dailyPriceRepository;
    }

    @Override
    public List<DailyPrice> fetchPricesAfter(String symbol, LocalDate lastDate) {
        if (lastDate == null) {
            return dailyPriceRepository.findBySymbolOrderByDateAsc(symbol);
        }
        return dailyPriceRepository.findBySymbolAndDateAfterOrderByDateAsc(symbol, lastDate);
    }
}