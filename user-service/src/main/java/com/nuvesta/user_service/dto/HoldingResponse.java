package com.nuvesta.user_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingResponse(String id, String symbol, LocalDate purchaseDate, BigDecimal priceAtPurchase) {}
