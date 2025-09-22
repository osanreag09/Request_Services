package co.com.crediya.api.filters;

import co.com.crediya.api.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class AuthorizationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public AuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String token = getTokenFromRequest(exchange.getRequest());
        if (token == null) {
            log.warn("Missing token in request to {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return jwtUtil.validateToken(token)
                .flatMap(claims -> {
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);

                    if (username == null || role == null) {
                        log.warn("Invalid token: missing username or role");
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // Check permissions based on path and method
                    if (!hasAccess(path, method, role)) {
                        log.warn("Access denied for user {} with role {} to {} {}",
                                username, role, method, path);
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                })
                .onErrorResume(e -> {
                    log.warn("Token validation failed: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean hasAccess(String path, String method, String userRole) {
        if ("/api/v1/solicitud".equals(path)) {
            if ("GET".equals(method)) {
                return "ADMIN".equals(userRole) || "ASSESSOR".equals(userRole);
            } else if ("POST".equals(method)) {
                return "ADMIN".equals(userRole) || "ASSESSOR".equals(userRole) || "CLIENT".equals(userRole);
            } else if ("PUT".equals(method)) {
                return "ADMIN".equals(userRole) || "ASSESSOR".equals(userRole);
            }
        }
        return true;
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/v1/login") ||
                path.equals("/api/v1/health") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars") ||
                path.startsWith("/swagger-resources") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".json") ||
                path.equals("/favicon.ico");
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        try {
            // Check for token in Authorization header (Bearer token)
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }

            // Check for token in custom 'token' header
            String tokenHeader = request.getHeaders().getFirst("token");
            if (tokenHeader != null && !tokenHeader.isEmpty()) {
                return tokenHeader.trim();
            }

            return null;
        } catch (Exception e) {
            log.warn("Error extracting token from request: {}", e.getMessage());
            return null;
        }
    }
}