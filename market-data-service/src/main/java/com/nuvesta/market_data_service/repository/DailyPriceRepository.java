package com.nuvesta.market_data_service.repository;

import com.nuvesta.market_data_service.model.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {
    boolean existsBySymbolAndDate(String symbol, LocalDate date);

    Optional<DailyPrice> findTopBySymbolOrderByDateDesc(String symbol);

    List<DailyPrice> findBySymbolOrderByDateAsc(String symbol);

    List<DailyPrice> findBySymbolAndDateAfterOrderByDateAsc(String symbol, LocalDate date);

    @Query(value = "SELECT dp.symbol, dp.close FROM daily_price dp INNER JOIN (SELECT symbol, MAX(date) AS max_date FROM daily_price WHERE symbol IN (:symbols) GROUP BY symbol) latest ON dp.symbol = latest.symbol AND dp.date = latest.max_date", nativeQuery = true)
    List<Object[]> findLatestCloseBySymbolIn(@Param("symbols") List<String> symbols);
}