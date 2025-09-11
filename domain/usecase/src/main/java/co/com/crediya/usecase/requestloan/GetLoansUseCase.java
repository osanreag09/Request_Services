package co.com.crediya.usecase.requestloan;

import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.gateways.StatesRepository;
import co.com.crediya.usecase.requestloan.gateways.GetLoans;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class GetLoansUseCase implements GetLoans {

    private final RequestsRepository requestsRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final StatesRepository loanStateRepository;

    public GetLoansUseCase(RequestsRepository requestsRepository,
                           LoanTypeRepository loanTypeRepository,
                           StatesRepository loanStateRepository) {
        this.requestsRepository = requestsRepository;
        this.loanTypeRepository = loanTypeRepository;
        this.loanStateRepository = loanStateRepository;
    }

    @Override
    public Flux<LoanRequests> execute(int init, int size) {
        List<Long> loanStateIds = List.of(1L, 3L, 6L);
        return requestsRepository.findByLoanStatesId(loanStateIds, init, size)
                .flatMap(request -> {
                    if (request.getLoanType() == null || request.getLoanState() == null) {
                        return Mono.error(new IllegalStateException("Loan request is missing type or state information"));
                    }

                    return Mono.zip(
                                    loanTypeRepository.findById(request.getLoanType().getId())
                                            .switchIfEmpty(Mono.error(new IllegalStateException("Loan type not found"))),
                                    loanStateRepository.findById(request.getLoanState().getId())
                                            .switchIfEmpty(Mono.error(new IllegalStateException("Loan state not found")))
                            )
                            .onErrorMap(e -> new IllegalStateException("Error fetching related data: " + e.getMessage()))
                            .map(tuple ->
                                         request
                                                .withLoanType(tuple.getT1())
                                                .withLoanState(tuple.getT2())

                            );
                })
                .onErrorResume(e -> Mono.empty());
    }
}
