package com.nuvesta.user_service.controller;

import com.nuvesta.user_service.dto.HoldingRequest;
import com.nuvesta.user_service.dto.HoldingResponse;
import com.nuvesta.user_service.dto.PortfolioRequest;
import com.nuvesta.user_service.dto.PortfolioResponse;
import com.nuvesta.user_service.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> listPortfolios() {
        return ResponseEntity.ok(portfolioService.listPortfolios());
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(@RequestBody @Valid PortfolioRequest request) {
        return ResponseEntity.ok(portfolioService.createPortfolio(request));
    }

    @PostMapping("/{portfolioId}/holdings")
    public ResponseEntity<HoldingResponse> addHolding(@PathVariable String portfolioId, @RequestBody @Valid HoldingRequest request) {
        return ResponseEntity.ok(portfolioService.addHolding(portfolioId, request));
    }

    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable String portfolioId){
        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{portfolioId}/holdings")
    public ResponseEntity<PortfolioResponse> clearHoldings(@PathVariable String portfolioId) {
        return ResponseEntity.ok(portfolioService.clearHoldings(portfolioId));
    }

    @DeleteMapping("/{portfolioId}/holdings/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable String portfolioId, @PathVariable String holdingId){
        portfolioService.deleteHolding(portfolioId, holdingId);
        return ResponseEntity.noContent().build();
    }
}
