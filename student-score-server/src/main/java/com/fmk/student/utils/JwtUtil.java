package com.fmk.student.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    // 注入秘钥和过期时间
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    /**
     * 生成安全秘钥
     */
    private SecretKey getSecretKey() {
        // 直接使用字符串的字节数组作为秘钥
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 去除前缀提取纯洁 Token 字符串
     */
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return bearerToken;
    }

    /**
     * 从 Claims 生成 Token
     */
    public String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并获取 Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 Token 是否有效 (未过期)
     */
    public boolean validateToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null && !claims.getExpiration().before(new Date());
    }

    /**
     * 获取用户名 (Subject 通常放账号名)
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }
}
