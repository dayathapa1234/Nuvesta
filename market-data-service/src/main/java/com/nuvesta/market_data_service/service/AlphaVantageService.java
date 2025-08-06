package com.nuvesta.market_data_service.service;

import com.nuvesta.market_data_service.model.SymbolInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class AlphaVantageService implements MarketDataService{

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<SymbolInfo> getAllSymbols() {
        String url = "https://www.alphavantage.co/query?function=LISTING_STATUS"+"&apikey=" + apiKey;
        String csv = restTemplate.getForObject(url,String.class);

        List<SymbolInfo> symbols = new ArrayList<>();

        if (csv == null || csv.isBlank()){
            return symbols;
        }

        try (BufferedReader reader = new BufferedReader( new StringReader(csv))){
            String line;
            boolean isFirst = true;

            while((line = reader.readLine()) != null){
                if (isFirst){
                    isFirst = false;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if(parts.length >=7 ){
                    symbols.add(new SymbolInfo(
                            parts[0], // symbol
                            parts[1], // name
                            parts[2], // exchange
                            parts[3], // assetType
                            parts[4], // ipoDate
                            parts[5], // delistingDate
                            parts[6]  // status
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Alpha Vantage CSV", e);
        }

        return symbols;
    }
}
