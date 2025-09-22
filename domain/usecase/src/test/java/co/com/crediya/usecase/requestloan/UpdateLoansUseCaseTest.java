package co.com.crediya.usecase.requestloan;

import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.NotificationService;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.model.states.gateways.StatesRepository;
import co.com.crediya.usecase.requestloan.exception.InvalidRequestDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateLoansUseCaseTest {

    @Mock
    private RequestsRepository requestsRepository;

    @Mock
    private StatesRepository loanStateRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UpdateLoansUseCase updateLoansUseCase;

    @Captor
    private ArgumentCaptor<LoanRequests> loanRequestsCaptor;

    private LoanState approvedState;
    private LoanState pendingState;
    private LoanState rejectedState;
    private LoanRequests pendingRequest;
    private LoanRequests updateRequest;

    @BeforeEach
    void setUp() {
        pendingState = LoanState.builder()
                .id(1L)
                .name("PENDING")
                .build();

        approvedState = LoanState.builder()
                .id(2L)
                .name("APPROVED")
                .build();

        rejectedState = LoanState.builder()
                .id(3L)
                .name("REJECTED")
                .build();

        pendingRequest = LoanRequests.builder()
                .id(1L)
                .email("test@example.com")
                .loanState(pendingState)
                .build();

        updateRequest = LoanRequests.builder()
                .id(1L)
                .email("test@example.com")
                .loanState(approvedState)
                .build();
    }

    @Test
    void execute_shouldUpdateLoanToApproved() {
        // Arrange
        when(loanStateRepository.findByName("APPROVED")).thenReturn(Mono.just(approvedState));
        when(requestsRepository.findById(1L)).thenReturn(Mono.just(pendingRequest));
        when(requestsRepository.update(any(LoanRequests.class))).thenReturn(Mono.just(updateRequest));
        when(notificationService.sendNotification(anyLong(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(updateLoansUseCase.execute(updateRequest))
                .expectNextMatches(updatedRequest -> 
                    updatedRequest.getId().equals(1L) && 
                    updatedRequest.getLoanState().getName().equals("APPROVED")
                )
                .verifyComplete();

        verify(loanStateRepository).findByName("APPROVED");
        verify(requestsRepository).findById(1L);
        verify(requestsRepository).update(loanRequestsCaptor.capture());
        verify(notificationService).sendNotification(1L, "APPROVED", "test@example.com");
    }

    @Test
    void execute_shouldUpdateLoanToRejected() {
        // Arrange
        LoanRequests rejectRequest = updateRequest.withLoanState(rejectedState);
        
        when(loanStateRepository.findByName("REJECTED")).thenReturn(Mono.just(rejectedState));
        when(requestsRepository.findById(1L)).thenReturn(Mono.just(pendingRequest));
        when(requestsRepository.update(any(LoanRequests.class))).thenReturn(Mono.just(rejectRequest));
        when(notificationService.sendNotification(anyLong(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(updateLoansUseCase.execute(rejectRequest))
                .expectNextMatches(updatedRequest -> 
                    updatedRequest.getId().equals(1L) && 
                    updatedRequest.getLoanState().getName().equals("REJECTED")
                )
                .verifyComplete();
    }

    @Test
    void execute_shouldFailWhenInvalidState() {
        // Arrange
        LoanRequests invalidRequest = updateRequest.withLoanState(
                LoanState.builder().name("INVALID").build()
        );

        // Act & Assert
        StepVerifier.create(updateLoansUseCase.execute(invalidRequest))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof InvalidRequestDataException);
                    assertEquals("Loan state must be either 'APPROVED' or 'REJECTED'", 
                            throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(requestsRepository, notificationService);
    }

    @Test
    void execute_shouldFailWhenStateNotFound() {
        // Arrange
        when(loanStateRepository.findByName("APPROVED")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(updateLoansUseCase.execute(updateRequest))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof InvalidRequestDataException);
                    assertTrue(throwable.getMessage().startsWith("Loan state not found: "));
                })
                .verify();

        verify(loanStateRepository).findByName("APPROVED");
        verifyNoMoreInteractions(loanStateRepository);
        verifyNoInteractions(requestsRepository, notificationService);
    }

    @Test
    void execute_shouldFailWhenRequestNotFound() {
        // Arrange
        when(loanStateRepository.findByName("APPROVED")).thenReturn(Mono.just(approvedState));
        when(requestsRepository.findById(1L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(updateLoansUseCase.execute(updateRequest))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof InvalidRequestDataException);
                    assertEquals("Loan request not found with id: 1", throwable.getMessage());
                })
                .verify();

        verify(requestsRepository).findById(1L);
        verifyNoMoreInteractions(requestsRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void execute_shouldNotSendNotificationWhenEmailIsMissing() {
        // Arrange
        LoanRequests requestWithoutEmail = pendingRequest.withEmail(null);
        LoanRequests updateRequestWithoutEmail = updateRequest.withEmail(null);
        
        when(loanStateRepository.findByName("APPROVED")).thenReturn(Mono.just(approvedState));
        when(requestsRepository.findById(1L)).thenReturn(Mono.just(requestWithoutEmail));
        when(requestsRepository.update(any(LoanRequests.class))).thenReturn(Mono.just(updateRequestWithoutEmail));

        // Act & Assert
        StepVerifier.create(updateLoansUseCase.execute(updateRequestWithoutEmail))
                .expectNextCount(1)
                .verifyComplete();

        verify(notificationService, never()).sendNotification(anyLong(), anyString(), anyString());
    }

    @Test
    void execute_shouldNotSendNotificationWhenEmailIsBlank() {
        // Arrange
        LoanRequests requestWithBlankEmail = pendingRequest.withEmail(" ");
        LoanRequests updateRequestWithBlankEmail = updateRequest.withEmail(" ");
        
        when(loanStateRepository.findByName("APPROVED")).thenReturn(Mono.just(approvedState));
        when(requestsRepository.findById(1L)).thenReturn(Mono.just(requestWithBlankEmail));
        when(requestsRepository.update(any(LoanRequests.class))).thenReturn(Mono.just(updateRequestWithBlankEmail));

        // Act & Assert
        StepVerifier.create(updateLoansUseCase.execute(updateRequestWithBlankEmail))
                .expectNextCount(1)
                .verifyComplete();

        verify(notificationService, never()).sendNotification(anyLong(), anyString(), anyString());
    }
}
