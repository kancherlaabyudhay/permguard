package com.permguard.util;

import com.permguard.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// ================================================================
//  JwtUtil — generate, parse, validate JWT tokens
//  ✅ FIX: added explicit constructor so @Value fields are injected
// ================================================================

@Component
public class JwtUtil {

    private final String jwtSecret;
    private final long   jwtExpirationMs;

    // ✅ FIX: constructor injection for @Value properties
    public JwtUtil(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.jwtSecret       = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // Build signing key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate JWT token
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId",             user.getUserId())
                .claim("role",               user.getRole().name())
                .claim("name",               user.getFullName())
                .claim("mustChangePassword", user.getMustChangePassword())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract email from token
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // Extract role from token
    public String getRoleFromToken(String token) {
        return (String) parseClaims(token).get("role");
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT unsupported: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("JWT malformed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT empty: " + e.getMessage());
        }
        return false;
    }

    // Parse claims
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}