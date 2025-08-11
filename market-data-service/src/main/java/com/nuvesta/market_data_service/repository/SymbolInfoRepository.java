package com.nuvesta.market_data_service.repository;

import com.nuvesta.market_data_service.model.SymbolInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SymbolInfoRepository extends JpaRepository<SymbolInfo, String> {
}