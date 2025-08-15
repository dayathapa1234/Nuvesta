package com.nuvesta.market_data_service.repository;

import com.nuvesta.market_data_service.model.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {
    boolean existsBySymbolAndDate(String symbol, LocalDate date);

    Optional<DailyPrice> findTopBySymbolOrderByDateDesc(String symbol);

    List<DailyPrice> findBySymbolOrderByDateAsc(String symbol);

    List<DailyPrice> findBySymbolAndDateAfterOrderByDateAsc(String symbol, LocalDate date);
}