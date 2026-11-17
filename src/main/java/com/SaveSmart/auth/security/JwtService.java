package com.SaveSmart.auth.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;

    @Value("${savesmart.jwt.expiration}")
    private long jwtExpiration;

    public JwtService(
            @Value("${savesmart.jwt.secret}") String secret) {

        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (DecodingException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET must be a Base64-encoded key of at least 32 bytes. "
                            + "Generate one with: openssl rand -base64 32",
                    exception
            );
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET is too short after Base64 decoding ("
                            + keyBytes.length
                            + " bytes). It must decode to at least 32 bytes. "
                            + "Generate one with: openssl rand -base64 32"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        return generateToken(email, "USER");
    }

    public String generateToken(String email, String role) {

        Date currentDate = new Date();

        Date expirationDate =
                new Date(currentDate.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("role", role == null ? "USER" : role)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRole(String token) {

        String role = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);

        return role == null || role.isBlank() ? "USER" : role;
    }
}