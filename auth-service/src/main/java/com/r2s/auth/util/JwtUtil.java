package com.r2s.auth.util;

import com.r2s.auth.entity.Role;
import com.r2s.auth.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtDuration;

    public String generateToken(final CustomUserDetails user) {
        Map<String, Object> claims = new HashMap<>();

        // Thêm authorities đúng định dạng
        claims.put("authorities", user.getAuthorities().stream().map(auth -> auth.getAuthority()) // ví dụ: "USER",
                // "ADMIN"
                .toList());

        // Bạn vẫn có thể giữ roles nếu cần dùng riêng
        claims.put("roles", user.getRoles().stream().map(Role::getName).toList());

        var expirationMillis = new Date(System.currentTimeMillis() + 1000 * jwtDuration); // 7 days

        return Jwts.builder().setClaims(claims).setSubject(user.getUsername()).setIssuedAt(new Date())
                .setExpiration(expirationMillis).signWith(this.getSignKey()).compact();
    }

    // tạo khóa bí mật
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // lấy thông tin từ token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // kiểm tra thời hạn
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // lấy thông tin từ token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // lấy thông tin từ token
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(this.getSignKey()).build().parseClaimsJws(token).getBody();
    }

    // kiểm tra thời hạn
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}