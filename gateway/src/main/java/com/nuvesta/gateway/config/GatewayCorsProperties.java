package com.nuvesta.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gateway.cors")
public class GatewayCorsProperties {
    private List<String> allowedOrigins = List.of("http://localhost:5173");

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return;
        }
        this.allowedOrigins = List.copyOf(allowedOrigins);
    }
}
