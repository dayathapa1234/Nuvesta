package com.nuvesta.user_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingRequest(
        @NotBlank(message = "Symbol is required")
        String symbol,

        @NotNull(message = "Purchase date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate purchaseDate,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be positive")
        BigDecimal quantity
) {}
