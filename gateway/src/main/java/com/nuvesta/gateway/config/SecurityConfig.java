package com.nuvesta.gateway.config;

import com.nuvesta.gateway.security.JwtReactiveAuthenticationManager;
import com.nuvesta.gateway.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig  {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         AuthenticationWebFilter jwtAuthenticationWebFilter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/auth/**", "/actuator/**", "/fallback/**").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                )
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public AuthenticationWebFilter jwtAuthenticationWebFilter(ReactiveAuthenticationManager authenticationManager, ServerAuthenticationConverter converter){
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager);
        filter.setServerAuthenticationConverter(converter);
        filter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.anyExchange());
        filter.setAuthenticationFailureHandler((exchange,ex) -> {
            exchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getExchange().getResponse().setComplete();
        });
        return filter;
    }

    @Bean
    public ServerAuthenticationConverter bearerTokenAuthenticationConverter() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> StringUtils.startsWithIgnoreCase(authHeader, "Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .map(BearerTokenAuthenticationToken::new);
    }

    @Bean
    public ReactiveAuthenticationManager jwtReactiveAuthenticationManager(JwtService jwtService){
        return new JwtReactiveAuthenticationManager(jwtService);
    }
}