package com.nuvesta.user_service.dto;

import jakarta.validation.constraints.NotBlank;

public record PortfolioRequest(
        @NotBlank(message = "Portfolio name is required")
        String name,
        String description
) {}