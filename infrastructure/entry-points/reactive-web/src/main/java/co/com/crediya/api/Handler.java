package co.com.crediya.api;

import co.com.crediya.api.dtos.LoanRequestsDTO;
import co.com.crediya.api.mappers.LoanRequestMapper;
import co.com.crediya.api.util.ValidationUtil;
import co.com.crediya.usecase.requestloan.gateways.RegistryRequestLoan;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final Validator validator;
    private final RegistryRequestLoan registryRequestLoan;


    public Mono<ServerResponse> registerRequest(ServerRequest request) {
        return request.bodyToMono(LoanRequestsDTO.class)
                .flatMap(dto -> ValidationUtil.validate(dto, validator))
                .map(LoanRequestMapper::toDomain)
                .flatMap(registryRequestLoan::execute)
                .map(LoanRequestMapper::toDTO)
                .flatMap(dtoResp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dtoResp));
    }
}
