package co.com.crediya.api.exceptions;

import co.com.crediya.usecase.requestloan.exception.InvalidRequestDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
    }

    @Test
    void handleAllUncaughtException_ShouldReturnInternalServerError() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        Mono<ResponseEntity<ErrorResponse>> result =
                globalExceptionHandler.handleAllUncaughtException(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().code()).isEqualTo("INTERNAL_SERVER_ERROR");
                    assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequest() {
        // Arrange
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        WebExchangeBindException ex = new WebExchangeBindException(null, bindingResult);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result =
                globalExceptionHandler.handleValidationExceptions(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().code()).isEqualTo("DATA_USER_INVALID");
                    assertThat(response.getBody().message()).isEqualTo("must not be null");
                })
                .verifyComplete();
    }

    @Test
    void handleInvalidRequestData_ShouldReturnBadRequest() {
        // Arrange
        String errorMessage = "Invalid request data";
        InvalidRequestDataException ex = new InvalidRequestDataException(errorMessage);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result =
                globalExceptionHandler.handleInvalidRequestData(ex);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().code()).isEqualTo("DATA_USER_INVALID");
                    assertThat(response.getBody().message()).isEqualTo(errorMessage);
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_ShouldReturnCorrectStatus() {
        // Arrange
        String errorMessage = "Not found";
        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result =
                globalExceptionHandler.handleResponseStatusException(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().code()).isEqualTo("ERROR_DATOS_USUARIO");
                    assertThat(response.getBody().message()).isEqualTo(errorMessage);
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_ShouldUseDefaultMessage_WhenReasonIsNull() {
        // Arrange
        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result =
                globalExceptionHandler.handleResponseStatusException(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().message()).isEqualTo("Validation error");
                })
                .verifyComplete();
    }
}