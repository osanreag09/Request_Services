package co.com.crediya.r2dbc.adapter;

import co.com.crediya.model.auth.UserInfo;
import co.com.crediya.model.auth.gateways.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class AuthServiceClientAdapter implements AuthServiceClient {

    private final WebClient webClient;
    private static final String AUTH_SERVICE_URL = "/api/v1/usuarios/";

    @Override
    public Mono<UserInfo> getUserInfo(String email, String token) {
        log.info("Is in auth service client adapter");
        log.info("Email: " + email);
        log.info("Token: " + token);
        return webClient.get()
                .uri(AUTH_SERVICE_URL + email)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Error sin mensaje")
                                .flatMap(errorBody ->
                                        handleError(response.statusCode(), errorBody)
                                )
                )
                .bodyToMono(UserInfo.class)
                .onErrorResume(WebClientResponseException.class, ex ->
                        handleError(ex.getStatusCode(), ex.getResponseBodyAsString())
                );
    }

    private <T> Mono<T> handleError(HttpStatusCode status, String message) {
        if (status.value() == HttpStatus.UNAUTHORIZED.value() ||
                status.value() == HttpStatus.FORBIDDEN.value()) {
            log.error("Access not authorized: " + message);
            return Mono.error(new SecurityException(""));
        }
        log.error("Error in authentication service (" + status + "): " + message);
        return Mono.error(new RuntimeException(""));
    }
}
