package com.nuvesta.market_data_service.config;

import com.nuvesta.market_data_service.model.SymbolInfo;
import com.nuvesta.market_data_service.repository.SymbolInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Profile("!test")
public class ConfiguredSymbolsSeeder {

    private static final Logger log = LoggerFactory.getLogger(ConfiguredSymbolsSeeder.class);

    @Bean
    CommandLineRunner seedConfiguredSymbols(SymbolInfoRepository repo,
                                            @Value("${yahoo.symbols:}") String yahooSymbols) {
        return args -> {
            List<String> symbols = parseSymbols(yahooSymbols);
            if (symbols.isEmpty()) return;

            for (String sym : symbols) {
                if (repo.existsById(sym)) continue;
                // Minimal stub; Yahoo fetch will enrich price table only.
                SymbolInfo si = new SymbolInfo(
                        sym,
                        sym,
                        "INDEX",
                        "INDEX",
                        null,
                        null,
                        "ACTIVE",
                        null
                );
                repo.save(si);
                log.info("Seeded missing configured symbol: {}", sym);
            }
        };
    }

    private static List<String> parseSymbols(String raw) {
        if (raw == null) return Collections.emptyList();
        String trim = raw.trim();
        if (trim.isEmpty() || trim.equalsIgnoreCase("full")) return Collections.emptyList();
        return Arrays.stream(trim.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

