package co.com.crediya.usecase.requestloan.gateways;

import co.com.crediya.model.requests.LoanRequests;
import reactor.core.publisher.Mono;

public interface UpdateLoans {
    Mono<LoanRequests> execute(LoanRequests loanRequests);
}
