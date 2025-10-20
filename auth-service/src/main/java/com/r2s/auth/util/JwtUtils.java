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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.duration}") // kĩ thuật đọc dữ liệu từ file propeties or yml.
    private long jwtDuration;// thời gian tồn tại của token

    //tạo token
    public String generateToken(final CustomUserDetails user){
        Map<String,Object>claims=new HashMap<>();
        claims.put("authorities",user.getAuthorities().stream().map(ath-> ath.getAuthority()).toList());
        claims.put("role",user.getRole().stream().map(Role::getRoleName).toList());
        var expirationMillis = new Date(System.currentTimeMillis() + 1000 * jwtDuration);

        return Jwts.builder().setClaims(claims).setSubject(user.getUsername()).setIssuedAt(new Date()).setExpiration(expirationMillis).signWith(this.getSignKey()).compact();
    }

    //tạo khóa bí mật
    private Key getSignKey(){
        byte[] keyBytes= Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //lấy thông tin userName
    //sau khi giải mã token thì lấy toàn bộ thông tin trong payload và ch lấy ra duy nhất useName
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    // sau khi giải mã token thì lấy toàn bộ thông tin trong payload và ch lấy ra duy nhất thời gian hết hạn
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    //lấy thông tin token nhưng chỉ lấy những phần cụ thể trong payload
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //lấy tất cả thông tin trong token
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(this.getSignKey()).build().parseClaimsJws(token).getBody();
    }
    //kiểm tra thời gian hết hạn(so sánh thoi gian hiện tại và thoi gian hết hạn)
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
