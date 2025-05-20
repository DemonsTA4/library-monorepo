package com.example.library.seurity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 强烈建议从配置文件读取，并且不要硬编码在代码中
    private final Key jwtSigningKey; // 或者使用更安全的密钥生成/管理方式
    private final long jwtExpirationInMs = 3600000; // 1 hour

    // 使用构造函数注入配置的密钥字符串
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 对于 HS256，密钥长度至少应为 256位 (32字节)。确保你的 secret 足够长。
        // Keys.hmacShaKeyFor(keyBytes) 是更推荐的方式，它会确保生成的密钥符合算法要求。
        this.jwtSigningKey = Keys.hmacShaKeyFor(keyBytes);
        // 或者，如果你严格控制字节长度并使用旧版API，可能是：
        // this.jwtSigningKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSigningKey).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 你可以添加更多 claims，例如用户的角色
        // List<String> roles = userDetails.getAuthorities().stream()
        //                        .map(GrantedAuthority::getAuthority)
        //                        .collect(Collectors.toList());
        // claims.put("roles", roles);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(jwtSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}