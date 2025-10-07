package com.nuvesta.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reactive {@link org.springframework.security.authentication.AuthenticationManager} that validates JWTs.
 */
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager{

    private final JwtService jwtService;

    public JwtReactiveAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = Objects.toString(authentication.getCredentials(), null);
        if (token == null){
            return Mono.empty();
        }
        return Mono.fromCallable(() -> jwtService.parseAndValidate(token))
                .map(claims -> buildAuthentication(claims, token))
                .onErrorMap(ex -> new BadCredentialsException("Invalid JWT token", ex));
    }

    private Authentication buildAuthentication(Claims claims, String token) {
        String subject = claims.getSubject();
        Collection<SimpleGrantedAuthority> authorities =
                extractRoles(claims).stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
        return new UsernamePasswordAuthenticationToken(subject, token, authorities);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof Collection<?>) {
            return ((Collection<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.singletonList("USER");
    }
}
