package co.com.crediya.usecase.requestloan;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.usecase.requestloan.gateways.GetLoans;
import reactor.core.publisher.Flux;

import java.util.List;

public class GetLoansUseCase implements GetLoans {

    private final RequestsRepository requestsRepository;

    public GetLoansUseCase(RequestsRepository requestsRepository) {
        this.requestsRepository = requestsRepository;

    }

    @Override
    public Flux<LoanRequests> execute(int init, int size) {
        List<Long> loanStateIds = List.of(1L, 3L, 6L);
        return requestsRepository.findByLoanStatesId(loanStateIds, init, size);
    }
}
