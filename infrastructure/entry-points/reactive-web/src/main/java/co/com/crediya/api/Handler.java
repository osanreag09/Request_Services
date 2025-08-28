package co.com.crediya.api;

import co.com.crediya.usecase.requestloan.gateways.RegistryRequestLoan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private final RegistryRequestLoan registryRequestLoan;

    public Mono<ServerResponse> listenGETUseCase(ServerRequest serverRequest) {

    }

}
