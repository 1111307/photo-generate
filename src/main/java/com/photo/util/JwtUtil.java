package com.photo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims);
    }

    /**
     * 生成token
     */
    private String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? Long.valueOf(claims.get("userId").toString()) : null;
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("username").toString() : null;
    }

    /**
     * 验证token是否过期
     */
    public Boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return true;
        }
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 判断token是否可以刷新（未过期或即将过期）
     * @param token 要检查的token
     * @param refreshThreshold 刷新阈值（毫秒），默认为token有效期的1/3
     * @return 是否可以刷新
     */
    public Boolean canTokenBeRefreshed(String token, Long refreshThreshold) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        Date expiration = claims.getExpiration();
        Date now = new Date();
        long timeToExpire = expiration.getTime() - now.getTime();
        // 如果token未过期且剩余时间小于阈值，则可以刷新
        return timeToExpire > 0 && timeToExpire < refreshThreshold;
    }

    /**
     * 判断token是否可以刷新（使用默认阈值：token有效期的1/3）
     */
    public Boolean canTokenBeRefreshed(String token) {
        return canTokenBeRefreshed(token, expiration / 3);
    }

    /**
     * 刷新token
     * @param token 旧token
     * @return 新token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        // 从旧token中提取用户信息
        Long userId = Long.valueOf(claims.get("userId").toString());
        String username = claims.get("username").toString();
        // 生成新token
        return generateToken(userId, username);
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}