package co.com.crediya.api.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;


@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public Mono<Claims> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Token cannot be null or empty"));
        }

        return Mono.fromCallable(() -> {
            try {
                return Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e) {
                log.warn("Error validating token: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid token: " + e.getMessage());
            }
        });
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    public static Mono<String> getTokenFromHeader(ServerRequest request) {
        String authHeader = request.headers().firstHeader("Authorization");
        String token = "";

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            log.error("Token not found in Authorization header");
            return Mono.error(new IllegalArgumentException("Token not found in Authorization header"));
        }
        return Mono.just(token);
    }
}