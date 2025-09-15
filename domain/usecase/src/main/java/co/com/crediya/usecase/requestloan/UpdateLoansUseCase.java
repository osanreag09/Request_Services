package co.com.crediya.usecase.requestloan;

import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.NotificationService;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.model.states.gateways.StatesRepository;
import co.com.crediya.usecase.requestloan.exception.InvalidRequestDataException;
import co.com.crediya.usecase.requestloan.gateways.UpdateLoans;
import reactor.core.publisher.Mono;

import java.util.Set;

public class UpdateLoansUseCase implements UpdateLoans {
    private static final Set<String> ALLOWED_STATES = Set.of("APPROVED", "REJECTED");
    private static final Long PENDING_STATE_ID = 1L;
    private static final String INVALID_STATE_MESSAGE = "Loan state must be either 'APPROVED' or 'REJECTED'";
    private static final String STATE_NOT_FOUND_MESSAGE = "Loan state not found: ";
    private static final String REQUEST_NOT_FOUND_MESSAGE = "Loan request not found with id: ";
    private static final String INVALID_REQUEST_STATE_MESSAGE = "Only PENDING loan requests can be updated. Current state ID: ";


    private final RequestsRepository requestsRepository;
    private final StatesRepository loanStateRepository;
    private final NotificationService notificationService;

    public UpdateLoansUseCase(RequestsRepository requestsRepository,
                              StatesRepository loanStateRepository,
                              NotificationService notificationService) {
        this.requestsRepository = requestsRepository;
        this.loanStateRepository = loanStateRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Mono<LoanRequests> execute(LoanRequests loanRequests) {
        String requestedStateName = loanRequests.getLoanState().getName();
        if (!ALLOWED_STATES.contains(requestedStateName.toUpperCase())) {
            return Mono.error(new InvalidRequestDataException(INVALID_STATE_MESSAGE));
        }

        return loanStateRepository.findByName(requestedStateName)
                .switchIfEmpty(Mono.error(new InvalidRequestDataException(
                        STATE_NOT_FOUND_MESSAGE + requestedStateName)))
                .flatMap(newState ->
                        requestsRepository.findById(loanRequests.getId())
                                .switchIfEmpty(Mono.error(new InvalidRequestDataException(
                                        REQUEST_NOT_FOUND_MESSAGE + loanRequests.getId())))
                                .flatMap(existingRequest ->
                                        validateRequestState(existingRequest)
                                                .then(updateRequest(existingRequest, newState))
                                )
                                .flatMap(this::sendNotificationIfNeeded)
                );
    }

    private Mono<Void> validateRequestState(LoanRequests request) {
        Long currentStateId = request.getLoanState().getId();

        if (!PENDING_STATE_ID.equals(currentStateId)) {
            String errorMessage = INVALID_REQUEST_STATE_MESSAGE + currentStateId;
            return Mono.error(new InvalidRequestDataException(errorMessage));
        }
        return Mono.empty();
    }

    private Mono<LoanRequests> updateRequest(LoanRequests request, LoanState newState) {
        return requestsRepository.update(request.withLoanState(newState));
    }


    private Mono<LoanRequests> sendNotificationIfNeeded(LoanRequests updatedRequest) {
        String userEmail = updatedRequest.getEmail();

        if (userEmail == null || userEmail.isBlank()) {
            System.out.printf("WARN: Cannot send notification - email not provided for request %d%n",
                    updatedRequest.getId());
            return Mono.just(updatedRequest);
        }

        return notificationService.sendNotification(
                        updatedRequest.getId(),
                        updatedRequest.getLoanState().getId().toString(),
                        userEmail
                )
                .doOnSuccess(messageId ->
                        System.out.printf("INFO: Notification enqueued to SQS. MessageId: %s, Request: %d, State: %s%n",
                                messageId,
                                updatedRequest.getId(),
                                updatedRequest.getLoanState().getId())
                )
                .onErrorResume(error -> {
                    System.err.printf("ERROR: Failed to send notification for request %d: %s%n",
                            updatedRequest.getId(),
                            error.getMessage());
                    return Mono.empty();
                })
                .thenReturn(updatedRequest);
    }
}
