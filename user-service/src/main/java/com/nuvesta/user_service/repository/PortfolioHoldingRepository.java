package com.nuvesta.user_service.repository;

import com.nuvesta.user_service.model.PortfolioHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, String> {
}
