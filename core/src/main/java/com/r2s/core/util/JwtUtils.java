package com.r2s.core.util;

import com.r2s.core.entity.Role;
import com.r2s.core.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.duration}") // ky thuat doc du lieu tu file properties or yml.
    private long jwtDuration;// thoi gian ton tai cua token

    //tạo token
    public String generateToken(final CustomUserDetails user){
        Map<String,Object>claims=new HashMap<>();
        claims.put("authorities",user.getAuthorities().stream().map(ath-> ath.getAuthority()).toList());
        claims.put("role",user.getRole().stream().map(Role::getRoleName).toList());
        Date expirationMillis = new Date(System.currentTimeMillis() + 1000 * jwtDuration);

        return Jwts.builder().setClaims(claims).setSubject(user.getUsername()).setIssuedAt(new Date()).setExpiration(expirationMillis).signWith(this.getSignKey()).compact();
    }

    //tạo khóa bí mật
    private Key getSignKey(){
        byte[] keyBytes= Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //lay thong tin userName
    //sau khi giai ma token thi lay toan bo thong tin trong payload va chi lay ra duy nhat userName
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    // sau khi giải mã token thì lấy toàn bộ thông tin trong payload và ch lấy ra duy nhất thời gian hết hạn
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    //lay thong tin token nhung chi lay nhung phan cu the trong payload
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //lay tat ca thong tin trong token
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(this.getSignKey()).build().parseClaimsJws(token).getBody();
    }
    //kiem tra thoi gian het han(so sanh thoi gian hien tai va thoi gian het han)
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    //validate token
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
