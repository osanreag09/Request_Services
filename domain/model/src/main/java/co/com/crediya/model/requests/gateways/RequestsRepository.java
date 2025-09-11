package co.com.crediya.model.requests.gateways;

import co.com.crediya.model.requests.LoanRequests;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RequestsRepository {
    Mono<LoanRequests> save(LoanRequests loanRequest);

    Mono<LoanRequests> findById(Long id);

    Flux<LoanRequests> findByLoanStatesId(List<Long> loanStateIds, int init, int size);
}
