package com.nuvesta.user_service.dto;

import java.time.Instant;
import java.util.List;

public record PortfolioResponse(
        String id,
        String name,
        String description,
        Instant createdAt,
        List<HoldingResponse> holdings
) {}
