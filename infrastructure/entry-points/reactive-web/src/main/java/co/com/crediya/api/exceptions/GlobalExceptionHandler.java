package co.com.crediya.api.exceptions;

import co.com.crediya.usecase.requestloan.exception.InvalidRequestDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private final String TIMESTAMP = "timestamp";
    private final String CODE = "code";
    private final String MESSAGE = "message";

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAllExceptions(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(CODE, "INTERNAL_SERVER_ERROR");
        errorResponse.put(MESSAGE, "Unexpected error. Please try again later.");
        errorResponse.put(TIMESTAMP, Instant.now().toString());

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptions(WebExchangeBindException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        String errorMessage = ex.getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(CODE, "ERROR_VALIDACION");
        errorResponse.put(MESSAGE, errorMessage);
        errorResponse.put(TIMESTAMP, Instant.now().toString());

        return Mono.just(ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(InvalidRequestDataException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInvalidRequestData(InvalidRequestDataException ex) {
        log.warn("Error in request data: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(CODE, "ERROR_DATOS_REQUEST");
        errorResponse.put(MESSAGE, ex.getMessage());
        errorResponse.put(TIMESTAMP, Instant.now().toString());

        return Mono.just(ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }
}
