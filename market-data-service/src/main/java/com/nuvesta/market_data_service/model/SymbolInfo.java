package com.nuvesta.market_data_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "symbol_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SymbolInfo {
    @Id
    private String symbol;
    private String name;
    private String exchange;
    private String assetType;
    private String ipoDate;
    private String delistingDate;
    private String status;

    @Transient
    private BigDecimal latestPrice;
}