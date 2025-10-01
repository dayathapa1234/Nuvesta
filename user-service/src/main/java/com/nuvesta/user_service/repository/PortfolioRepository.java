package com.nuvesta.user_service.repository;

import com.nuvesta.user_service.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, String> {
    List<Portfolio> findAllByUserId(String userId);
}
