package com.nuvesta.market_data_service.repository;

import com.nuvesta.market_data_service.model.SymbolInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SymbolInfoRepository extends JpaRepository<SymbolInfo, String>, JpaSpecificationExecutor<SymbolInfo> {
    @Query("SELECT DISTINCT s.exchange FROM SymbolInfo s WHERE s.exchange IS NOT NULL AND s.exchange <> '' ORDER BY s.exchange")
    List<String> findDistinctExchanges();

    @Query("SELECT DISTINCT s.assetType FROM SymbolInfo s WHERE s.assetType IS NOT NULL AND s.assetType <> '' ORDER BY s.assetType")
    List<String> findDistinctAssetTypes();

    @Query("SELECT DISTINCT s.ipoDate FROM SymbolInfo s WHERE s.ipoDate IS NOT NULL AND s.ipoDate <> '' ORDER BY s.ipoDate")
    List<String> findDistinctIpoDates();
}