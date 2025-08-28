package co.com.crediya.usecase.requestloan;

import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.LoanState;
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
            StatesRepository loanRequestsRepository
    ) {
        this.typeRepository = typeRepository;
        this.requestsRepository = requestsRepository;
        this.statesRepository = loanRequestsRepository;
    }

    public Mono<LoanRequests> execute(LoanRequests loanRequests) {
        return validateLoanType(loanRequests.getLoanType().getName())
                .flatMap(isValid -> {
                    if (Boolean.FALSE.equals(isValid)) {
                        return Mono.error(new InvalidRequestDataException("Loan type not found"));
                    }
                    return requestsRepository.save(loanRequests.withLoanState(
                            LoanState.builder().id(1L).name("PENDING_REVISION").build()
                    ));
                });
    }

    private Mono<Boolean> validateLoanType(String loanTypeName) {
        if (loanTypeName == null || loanTypeName.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Loan type name cannot be empty"));
        }
        return typeRepository.existsByName(loanTypeName);
    }

    /*
    public LoanTypeDetails calculateLoanDetails(String loanTypeName, int term, double amount) {
        LoanTypeDetails details = new LoanTypeDetails();
        details.setName(loanTypeName);

        switch (loanTypeName.toUpperCase()) {
            case "HOME":
                details.setMinimumAmount(50000.0);
                details.setMaximumAmount(500000.0);
                details.setInterestRate(term > 120 ? 0.07 : 0.05); // Mayor plazo, mayor interés
                details.setAutomaticValidation(amount <= 300000);
                break;

            case "PERSONAL":
                details.setMinimumAmount(1000.0);
                details.setMaximumAmount(50000.0);
                details.setInterestRate(term > 36 ? 0.12 : 0.10);
                details.setAutomaticValidation(amount <= 20000);
                break;

            case "AUTO":
                details.setMinimumAmount(5000.0);
                details.setMaximumAmount(100000.0);
                details.setInterestRate(term > 60 ? 0.09 : 0.07);
                details.setAutomaticValidation(amount <= 60000);
                break;

            case "EDUCATION":
                details.setMinimumAmount(1000.0);
                details.setMaximumAmount(20000.0);
                details.setInterestRate(0.05); // Tasa fija
                details.setAutomaticValidation(true); // Siempre validación automática
                break;

            default:
                throw new IllegalArgumentException("Tipo de préstamo no soportado: " + loanTypeName);
        }

        return details;
    }
     */

    /* TODO: Delte this method
    private Mono<LoanRequests> getLoanType(Long requestLoanTypeId) {
        return requestsRepository.findById(requestLoanTypeId)
                .flatMap(loanRequests -> {
                    Mono<LoanState> state = statesRepository.findById(loanRequests.getLoanState().getId());
                    Mono<LoanType> type = typeRepository.findById(loanRequests.getLoanType().getId());
                    return Mono.zip(Mono.just(loanRequests), state, type)
                            .map(tuple -> {
                                LoanRequests loanRequest = tuple.getT1();
                                loanRequest.setLoanState(tuple.getT2());
                                loanRequest.setLoanType(tuple.getT3());
                                return loanRequest;
                            });

                });
    }
     */

}
