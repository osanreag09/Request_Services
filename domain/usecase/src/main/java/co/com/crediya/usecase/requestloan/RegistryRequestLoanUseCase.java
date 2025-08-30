package co.com.crediya.usecase.requestloan;

import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.gateways.StatesRepository;
import co.com.crediya.usecase.requestloan.exception.InvalidRequestDataException;
import co.com.crediya.usecase.requestloan.gateways.RegistryRequestLoan;
import reactor.core.publisher.Mono;


public class RegistryRequestLoanUseCase implements RegistryRequestLoan {
    private final LoanTypeRepository typeRepository;
    private final RequestsRepository requestsRepository;
    private final StatesRepository statesRepository;

    public RegistryRequestLoanUseCase(
            LoanTypeRepository typeRepository,
            RequestsRepository requestsRepository,
            StatesRepository statesRepository
    ) {
        this.typeRepository = typeRepository;
        this.requestsRepository = requestsRepository;
        this.statesRepository = statesRepository;
    }

    public Mono<LoanRequests> execute(LoanRequests loanRequests) {
        return validateLoanType(loanRequests.getLoanType().getName())
                .then(statesRepository.findByName("PENDING")
                        .switchIfEmpty(Mono.error(new InvalidRequestDataException("PENDING state not found")))
                        .flatMap(state ->
                                typeRepository.findByName(loanRequests.getLoanType().getName())
                                        .switchIfEmpty(Mono.error(new InvalidRequestDataException("Loan type not found")))
                                        .flatMap(loanType -> {
                                            LoanRequests requestToSave = loanRequests
                                                    .withLoanState(state)
                                                    .withLoanType(loanType);
                                            return requestsRepository.save(requestToSave);
                                        })
                        )
                );
    }

    private Mono<Void> validateLoanType(String loanTypeName) {
        if (loanTypeName == null || loanTypeName.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Loan type name cannot be empty"));
        }
        return typeRepository.existsByName(loanTypeName)
                .handle((exists, sink) -> {
                    if (Boolean.FALSE.equals(exists)) {
                        sink.error(new InvalidRequestDataException("Loan type not found"));
                    } else {
                        sink.complete();
                    }
                });
    }

    /* //TODO: Delte this method
     private Mono<LoanRequests> getLoanType(Long requestLoanTypeId) {
         return requestsRepository.findById(requestLoanTypeId)
                 .flatMap(loanRequests ->
                         statesRepository.findById(loanRequests.getLoanState().getId())
                                 .flatMap(state ->
                                         typeRepository.findById(loanRequests.getLoanType().getId())
                                                 .map(type -> loanRequests
                                                         .withLoanState(state)
                                                         .withLoanType(type)
                                                 )
                                 )
                 );
     }
     */
}
