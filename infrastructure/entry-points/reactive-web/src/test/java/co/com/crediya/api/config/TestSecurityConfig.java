package co.com.crediya.api.config;


import co.com.crediya.model.auth.UserInfo;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges ->
                        exchanges.anyExchange().permitAll()
                )
                .securityContextRepository(securityContextRepository())
                .build();
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new ServerSecurityContextRepository() {
            @Override
            public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
                return Mono.empty();
            }

            @Override
            public Mono<SecurityContext> load(ServerWebExchange exchange) {
                return Mono.just(createSecurityContext());
            }
        };
    }

    private SecurityContext createSecurityContext() {
        UserInfo userInfo = UserInfo.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role("USER")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userInfo,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return new SecurityContextImpl(authentication);
    }
}
