package co.com.crediya.usecase.requestloan.gateways;

import co.com.crediya.model.requests.LoanRequests;
import reactor.core.publisher.Flux;

public interface GetLoans {
    Flux<LoanRequests> execute(int init, int size);
}
