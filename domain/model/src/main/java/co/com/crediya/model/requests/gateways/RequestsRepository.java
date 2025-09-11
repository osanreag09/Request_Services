package co.com.crediya.model.requests.gateways;

import co.com.crediya.model.requests.LoanRequests;
import reactor.core.publisher.Mono;

public interface RequestsRepository {
    Mono<LoanRequests> save(LoanRequests loanRequest);

    Mono<LoanRequests> findById(Long id);
}
