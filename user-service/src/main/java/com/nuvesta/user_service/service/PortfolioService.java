package com.nuvesta.user_service.service;

import com.nuvesta.user_service.dto.HoldingRequest;
import com.nuvesta.user_service.dto.HoldingResponse;
import com.nuvesta.user_service.dto.PortfolioRequest;
import com.nuvesta.user_service.dto.PortfolioResponse;
import com.nuvesta.user_service.model.Portfolio;
import com.nuvesta.user_service.model.PortfolioHolding;
import com.nuvesta.user_service.model.UserAccount;
import com.nuvesta.user_service.repository.PortfolioHoldingRepository;
import com.nuvesta.user_service.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final CurrentUserProvider currentUserProvider;

    public PortfolioService(PortfolioRepository portfolioRepository, PortfolioHoldingRepository portfolioHoldingRepository, CurrentUserProvider currentUserProvider) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioHoldingRepository = portfolioHoldingRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioRequest request){
        UserAccount user = currentUserProvider.getCurrentUser();
        Portfolio portfolio = Portfolio.builder()
                .name(request.name())
                .description(request.description())
                .user(user)
                .build();
        Portfolio saved = portfolioRepository.save(portfolio);
        user.getPortfolios().add(saved);
        return mapPortfolio(saved);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> listPortfolios(){
        UserAccount user = currentUserProvider.getCurrentUser();
        return portfolioRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::mapPortfolio)
                .collect(Collectors.toList());
    }

    @Transactional
    public HoldingResponse addHolding(String portfolioId, HoldingRequest request){
        Portfolio portfolio = getOwnedPortfolio(portfolioId);
        PortfolioHolding holding = PortfolioHolding.builder()
                .portfolio(portfolio)
                .symbol(request.symbol().toUpperCase())
                .purchaseDate(request.purchaseDate())
                .priceAtPurchase(request.price())
                .build();
        PortfolioHolding saved = portfolioHoldingRepository.save(holding);
        portfolio.getHoldings().add(saved);
        return mapHolding(saved);
    }

    @Transactional
    public void deletePortfolio(String portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(portfolioId);
        portfolioRepository.delete(portfolio);
    }

    @Transactional
    public PortfolioResponse clearHoldings(String portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(portfolioId);
        if (!portfolio.getHoldings().isEmpty()) {
            portfolio.getHoldings().clear();
        }
        return mapPortfolio(portfolio);
    }

    @Transactional
    public void deleteHolding(String portfolioId, String holdingId) {
        Portfolio portfolio = getOwnedPortfolio(portfolioId);
        PortfolioHolding holding = portfolioHoldingRepository.findByIdAndPortfolioId(holdingId, portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found in portfolio"));
        portfolio.getHoldings().removeIf(existing -> existing.getId().equals(holdingId));
        portfolioHoldingRepository.delete(holding);
    }

    private Portfolio getOwnedPortfolio(String portfolioId){
        UserAccount user = currentUserProvider.getCurrentUser();
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));
        if (!portfolio.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("You do not have access to this portfolio");
        }
        return portfolio;
    }

    public PortfolioResponse mapPortfolio(Portfolio portfolio){
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getCreatedAt(),
                portfolio.getHoldings().stream().map(this::mapHolding).toList()
        );
    }

    private HoldingResponse mapHolding(PortfolioHolding holding) {
        return new HoldingResponse(
                holding.getId(),
                holding.getSymbol(),
                holding.getPurchaseDate(),
                holding.getPriceAtPurchase()
        );
    }
}
