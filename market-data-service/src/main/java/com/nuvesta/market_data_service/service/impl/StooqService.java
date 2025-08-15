package com.nuvesta.market_data_service.service.impl;

import com.nuvesta.market_data_service.model.DailyPrice;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class StooqService {

    private final RestTemplate restTemplate;

    public StooqService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public List<DailyPrice> fetchPricesAfter(String symbol, LocalDate lastDate) {
        String url = "https://stooq.com/q/d/l/?s=" + symbol.toLowerCase() + ".us&i=d";
        String body = restTemplate.getForObject(url, String.class);
        if (body == null || body.isBlank()) {
            return List.of();
        }
        List<DailyPrice> prices = new ArrayList<>();
        Arrays.stream(body.trim().split("\n"))
                .skip(1)
                .map(line -> line.split(","))
                .filter(tokens -> tokens.length >= 6)
                .forEach(tokens -> {
                    LocalDate date = LocalDate.parse(tokens[0]);
                    if (lastDate == null || date.isAfter(lastDate)) {
                        DailyPrice price = new DailyPrice();
                        price.setSymbol(symbol.toUpperCase());
                        price.setDate(date);
                        price.setOpen(new BigDecimal(tokens[1]));
                        price.setHigh(new BigDecimal(tokens[2]));
                        price.setLow(new BigDecimal(tokens[3]));
                        price.setClose(new BigDecimal(tokens[4]));
                        price.setVolume(Long.parseLong(tokens[5]));
                        prices.add(price);
                    }
                });
        return prices;
    }
}