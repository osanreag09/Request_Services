package co.com.crediya.r2dbc;

import co.com.crediya.model.auth.UserInfo;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private AuthServiceClientAdapter authServiceClient;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TOKEN = "test-token";
    private static final UserInfo TEST_USER = new UserInfo(1L, TEST_EMAIL, "Test User", "USER");

    @BeforeEach
    void setUp() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        authServiceClient = new AuthServiceClientAdapter(webClient);
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenRequestIsSuccessful() {
        // Arrange
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserInfo.class)).thenReturn(Mono.just(TEST_USER));

        // Act & Assert
        StepVerifier.create(authServiceClient.getUserInfo(TEST_EMAIL, TEST_TOKEN))
                .expectNext(TEST_USER)
                .verifyComplete();
    }

    @Test
    void getUserInfo_ShouldThrowSecurityException_WhenUnauthorized() {
        // Arrange
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.error(WebClientResponseException.create(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        null, null, null
                )));

        // Act & Assert
        StepVerifier.create(authServiceClient.getUserInfo(TEST_EMAIL, TEST_TOKEN))
                .expectError(SecurityException.class)
                .verify();
    }

    @Test
    void getUserInfo_ShouldHandleWebClientError() {
        // Arrange
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.error(new RuntimeException("Internal Server Error")));

        // Act & Assert
        StepVerifier.create(authServiceClient.getUserInfo(TEST_EMAIL, TEST_TOKEN))
                .expectError(RuntimeException.class)
                .verify();
    }
}