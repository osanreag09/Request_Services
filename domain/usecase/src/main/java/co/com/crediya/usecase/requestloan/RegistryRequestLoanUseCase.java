package co.com.crediya.usecase.requestloan;

import co.com.crediya.model.auth.gateways.AuthServiceClient;
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
    private final AuthServiceClient authServiceClient;

    public RegistryRequestLoanUseCase(
            LoanTypeRepository typeRepository,
            RequestsRepository requestsRepository,
            StatesRepository statesRepository,
            AuthServiceClient authServiceClient
    ) {
        this.typeRepository = typeRepository;
        this.requestsRepository = requestsRepository;
        this.statesRepository = statesRepository;
        this.authServiceClient = authServiceClient;
    }

    public Mono<LoanRequests> execute(LoanRequests loanRequests, String token) {
        return makeValidation(loanRequests.getLoanType().getName(), loanRequests.getEmail(), token)
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

    private Mono<Void> validateUser(String email, String token) {
        return authServiceClient.getUserInfo(email, token)
                .switchIfEmpty(Mono.error(new InvalidRequestDataException("User not found")))
                .flatMap(userInfo -> {
                    if (!userInfo.email().equals(email)) {
                        return Mono.error(new InvalidRequestDataException("Email does not match user record"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> makeValidation(String loanTypeName, String email, String token) {
        return validateLoanType(loanTypeName)
                .then(validateUser(email, token));
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
