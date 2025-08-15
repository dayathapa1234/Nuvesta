package com.nuvesta.market_data_service.service;

import com.nuvesta.market_data_service.model.DailyPrice;

import java.time.LocalDate;
import java.util.List;

public interface PriceService {
    List<DailyPrice> fetchPricesAfter(String symbol, LocalDate lastDate);
}