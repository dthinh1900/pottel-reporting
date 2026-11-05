package vn.pottel.reporting.security;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret phải có ít nhất 32 byte."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    // Tạo JWT cho tài khoản đã đăng nhập
    public String generateToken(UserDetails userDetails) {

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    // Lấy username từ JWT
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Kiểm tra JWT có hợp lệ với tài khoản không
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        try {
            Claims claims = extractAllClaims(token);

            return claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Giải mã và xác minh chữ ký JWT
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}