package co.com.crediya.api;

import co.com.crediya.api.dtos.LoanRequestsDTO;
import co.com.crediya.api.mappers.LoanRequestMapper;
import co.com.crediya.api.util.JwtUtil;
import co.com.crediya.api.util.ValidationUtil;
import co.com.crediya.usecase.requestloan.gateways.GetLoans;
import co.com.crediya.usecase.requestloan.gateways.RegistryRequestLoan;
import co.com.crediya.usecase.requestloan.gateways.UpdateLoans;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final Validator validator;
    private final RegistryRequestLoan registryRequestLoan;
    private final GetLoans getLoans;
    private final UpdateLoans updateLoans;

    public Mono<ServerResponse> registerRequest(ServerRequest request) {
        return JwtUtil.getTokenFromHeader(request)
                .flatMap(token ->
                        request.bodyToMono(LoanRequestsDTO.class)
                                .flatMap(dto -> ValidationUtil.validate(dto, validator))
                                .map(LoanRequestMapper::toDomain)
                                .flatMap(loanRequests -> registryRequestLoan.execute(loanRequests, token))
                                .map(LoanRequestMapper::toDTO)
                                .flatMap(dtoResp -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(dtoResp))
                );
    }

    public Mono<ServerResponse> getAllRequests(ServerRequest request) {
        int init = Integer.parseInt(request.queryParam("init").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        return getLoans.execute(init, size)
                .transform(LoanRequestMapper::toDTOList)
                .collectList()
                .flatMap(loans -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loans));
    }

    public Mono<ServerResponse> updateRequest(ServerRequest request) {
        return request.bodyToMono(LoanRequestsDTO.class)
                        .map(LoanRequestMapper::toDomain)
                        .flatMap(updateLoans::execute)
                        .map(LoanRequestMapper::toDTO)
                        .flatMap(dtoResp -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(dtoResp));
    }

}
