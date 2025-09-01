package co.com.crediya.r2dbc.config;

import co.com.crediya.model.auth.gateways.AuthServiceClient;
import co.com.crediya.r2dbc.adapter.AuthServiceClientAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AuthServiceConfig {

    @Bean
    public WebClient authServiceWebClient(
            @Value("${auth.service.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public AuthServiceClient authServiceClient(
            @Qualifier("authServiceWebClient") WebClient authWebClient) {
        return new AuthServiceClientAdapter(authWebClient);
    }
}
