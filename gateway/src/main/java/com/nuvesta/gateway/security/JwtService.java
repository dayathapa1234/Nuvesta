package com.nuvesta.gateway.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * Minimal JWT helper that validates and exposes token claims using the shared signing secret.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secret){
        byte[] keyBytes = Decoders.BASE64URL.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseAndValidate(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.toInstant().isBefore(Instant.now())){
            throw new IllegalStateException("JWT has expired");
        }
        return claims;
    }
}
