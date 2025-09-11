package co.com.crediya.usecase;

import co.com.crediya.model.auth.UserInfo;
import co.com.crediya.model.auth.gateways.AuthServiceClient;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.model.states.gateways.StatesRepository;
import co.com.crediya.usecase.requestloan.RegistryRequestLoanUseCase;
import co.com.crediya.usecase.requestloan.exception.InvalidRequestDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistryRequestLoanUseCaseTest {

    @Mock
    private LoanTypeRepository typeRepository;

    @Mock
    private RequestsRepository requestsRepository;

    @Mock
    private StatesRepository statesRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private RegistryRequestLoanUseCase useCase;

    private LoanRequests testLoanRequest;
    private LoanState pendingState;
    private LoanType testLoanType;

    @BeforeEach
    void setUp() {
        pendingState = LoanState.builder()
                .id(1L)
                .name("PENDING")
                .build();

        testLoanType = LoanType.builder()
                .id(1L)
                .name("PERSONAL")
                .build();

        testLoanRequest = LoanRequests.builder()
                .id(1L)
                .amount(1000.0)
                .term(12)
                .email("test@example.com")
                .loanType(testLoanType)
                .loanState(pendingState)
                .build();
    }

    @Test
    void execute_ShouldSaveLoanRequest_WhenValidData() {
        // Arrange
        when(typeRepository.existsByName(anyString())).thenReturn(Mono.just(true));
        when(statesRepository.findByName("PENDING")).thenReturn(Mono.just(pendingState));
        when(typeRepository.findByName(anyString())).thenReturn(Mono.just(testLoanType));
        when(requestsRepository.save(any(LoanRequests.class))).thenReturn(Mono.just(testLoanRequest));
        when(authServiceClient.getUserInfo(anyString()))
                .thenReturn(Mono.just(new UserInfo(
                        1L,"test@example.com",
                        "test user",
                        "USER")));
        // Act & Assert
        StepVerifier.create(useCase.execute(testLoanRequest))
                .expectNextMatches(savedRequest ->
                        savedRequest.getId().equals(1L) &&
                                savedRequest.getEmail().equals("test@example.com") &&
                                savedRequest.getLoanType().getName().equals("PERSONAL") &&
                                savedRequest.getLoanState().getName().equals("PENDING")
                )
                .verifyComplete();
    }

    @Test
    void execute_ShouldFail_WhenLoanTypeNotExists() {
        // Arrange
        when(typeRepository.existsByName(anyString())).thenReturn(Mono.just(false));
        when(statesRepository.findByName("PENDING")).thenReturn(Mono.just(pendingState));
        when(authServiceClient.getUserInfo(anyString()))
                .thenReturn(Mono.just(new UserInfo(
                        1L,"test@example.com",
                        "test user",
                        "USER")));

        // Act & Assert
        StepVerifier.create(useCase.execute(testLoanRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidRequestDataException &&
                                throwable.getMessage().equals("Loan type not found")
                )
                .verify();
    }

    @Test
    void execute_ShouldFail_WhenPendingStateNotFound() {
        // Arrange
        when(typeRepository.existsByName(anyString())).thenReturn(Mono.just(true));
        when(statesRepository.findByName("PENDING")).thenReturn(Mono.empty());
        when(authServiceClient.getUserInfo(anyString()))
                .thenReturn(Mono.just(new UserInfo(
                        1L,"test@example.com",
                        "test user",
                        "USER")));

        // Act & Assert
        StepVerifier.create(useCase.execute(testLoanRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidRequestDataException &&
                                throwable.getMessage().equals("PENDING state not found")
                )
                .verify();
    }

    @Test
    void execute_ShouldFail_WhenLoanTypeIsEmpty() {
        // Arrange
        testLoanRequest.getLoanType().setName("");
        when(statesRepository.findByName("PENDING")).thenReturn(Mono.just(pendingState));
        when(authServiceClient.getUserInfo(anyString()))
                .thenReturn(Mono.just(new UserInfo(
                        1L,"test@example.com",
                        "test user",
                        "USER")));

        // Act & Assert
        StepVerifier.create(useCase.execute(testLoanRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Loan type name cannot be empty")
                )
                .verify();
    }

    @Test
    void validateLoanType_ShouldFail_WhenLoanTypeIsNull() {
        // Arrange
        testLoanRequest.getLoanType().setName(null);
        when(statesRepository.findByName("PENDING")).thenReturn(Mono.just(pendingState));
        when(authServiceClient.getUserInfo(anyString()))
                .thenReturn(Mono.just(new UserInfo(
                        1L,"test@example.com",
                        "test user",
                        "USER")));

        // Act & Assert
        StepVerifier.create(useCase.execute(testLoanRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Loan type name cannot be empty")
                )
                .verify();
    }

    @Test
    void validateUser_ShouldFail_WhenEmailDoesNotMatch() {
        // Arrange
        String requestEmail = "different@example.com";
        String userEmail = "stored@example.com";

        // Set up test data with a different email
        testLoanRequest = testLoanRequest.withEmail(requestEmail);

        // Mock the repository calls
        when(typeRepository.existsByName(anyString())).thenReturn(Mono.just(true));
        when(statesRepository.findByName("PENDING")).thenReturn(Mono.just(pendingState));

        // Mock the auth service to return a user with a different email
        when(authServiceClient.getUserInfo(requestEmail))
                .thenReturn(Mono.just(new UserInfo(
                        1L,
                        userEmail,  // Different from request email
                        "test user",
                        "USER"
                )));

        // Act & Assert
        StepVerifier.create(useCase.execute(testLoanRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidRequestDataException &&
                                throwable.getMessage().contains("Email does not match user record")
                )
                .verify();
    }
}
