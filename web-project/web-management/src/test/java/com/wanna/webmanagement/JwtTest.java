package com.wanna.webmanagement;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JwtTest {

    private static final String secret = "5rWL6K+V5L2/55So55qE5a+G5YyZ";

    @Test
    public void testJwt() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // ≥32 字节
        String jwt = Jwts.builder()
                .subject("1")
                .claim("username", "admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)          // key 是 HmacSHA256 → 自动选 HS256
                .compact();

        Claims c = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(jwt).getPayload();
        assertEquals("1", c.getSubject());
        assertEquals("admin", c.get("username", String.class));
    }
}
