package co.com.crediya.r2dbc;

import co.com.crediya.model.auth.UserInfo;
import co.com.crediya.model.auth.gateways.AuthServiceClient;
import co.com.crediya.r2dbc.adapter.AuthServiceClientAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientAdapterTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AuthServiceClient authServiceClient;
    private final String basePath = "/api/v1/usuarios/";

    @BeforeEach
    void setUp() {
        authServiceClient = new AuthServiceClientAdapter(webClient) {
        };
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenRequestIsSuccessful() {
        // Arrange
        String email = "test@example.com";
        UserInfo expectedUser = new UserInfo(1L, email, "Test User", "USER");
        mockWebClientSuccess(expectedUser, email);

        // Act & Assert
        StepVerifier.create(authServiceClient.getUserInfo(email))
                .expectNext(expectedUser)
                .verifyComplete();
    }

    @Test
    void getUserInfo_ShouldReturnEmpty_WhenUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        mockWebClientError(email, HttpStatus.NOT_FOUND);

        // Act & Assert
        StepVerifier.create(authServiceClient.getUserInfo(email))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Error en el servicio de autenticación (404 NOT_FOUND)"))
                .verify();
    }

    @Test
    void getUserInfo_ShouldThrowSecurityException_WhenUnauthorized() {
        // Arrange
        String email = "unauthorized@example.com";
        mockWebClientError(email, HttpStatus.UNAUTHORIZED);

        // Act & Assert
        StepVerifier.create(authServiceClient.getUserInfo(email))
                .expectErrorMatches(throwable ->
                        throwable instanceof SecurityException &&
                                throwable.getMessage().contains("Acceso no autorizado"))
                .verify();
    }

    @Test
    void getUserInfo_ShouldHandleWebClientError() {
        // Arrange
        String email = "error@example.com";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(basePath + email)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.error(WebClientResponseException.create(
                       500,
                        "Internal Server Error",
                        null, null, null
                )));

        // Act & Assert
        StepVerifier.create(authServiceClient.getUserInfo(email))
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(RuntimeException.class);
                    assertThat(throwable.getMessage()).contains("Error en el servicio de autenticación (500 INTERNAL_SERVER_ERROR)");
                })
                .verify();
    }

    private void mockWebClientSuccess(UserInfo userInfo, String email) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(basePath + email)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserInfo.class)).thenReturn(Mono.just(userInfo));
    }

    private void mockWebClientError(String email, HttpStatus status) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(basePath + email)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.error(WebClientResponseException.create(
                        status.value(),
                        status.getReasonPhrase(),
                        null, null, null
                )));
    }
}