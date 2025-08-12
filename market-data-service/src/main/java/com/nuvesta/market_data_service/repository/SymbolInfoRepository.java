package com.nuvesta.market_data_service.repository;

import com.nuvesta.market_data_service.model.SymbolInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SymbolInfoRepository extends JpaRepository<SymbolInfo, String>, JpaSpecificationExecutor<SymbolInfo> {
}