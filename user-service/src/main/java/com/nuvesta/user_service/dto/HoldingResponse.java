package com.nuvesta.user_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingResponse(
        String id,
        String symbol,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate purchaseDate,
        BigDecimal priceAtPurchase
) {}
