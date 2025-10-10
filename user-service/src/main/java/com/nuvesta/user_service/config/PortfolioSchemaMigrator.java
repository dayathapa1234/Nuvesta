package com.nuvesta.user_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PortfolioSchemaMigrator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PortfolioSchemaMigrator.class);

    private final JdbcTemplate jdbcTemplate;

    public PortfolioSchemaMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS portfolio_holdings
                    ADD COLUMN IF NOT EXISTS quantity numeric(19,4)
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS portfolio_holdings
                    ALTER COLUMN quantity SET DEFAULT 1
                    """);

            jdbcTemplate.execute("""
                    UPDATE portfolio_holdings
                    SET quantity = 1
                    WHERE quantity IS NULL
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS portfolio_holdings
                    ALTER COLUMN quantity SET NOT NULL
                    """);
        } catch (Exception ex) {
            log.warn("Failed to ensure portfolio_holdings.quantity column is present: {}", ex.getMessage());
        }
    }
}
