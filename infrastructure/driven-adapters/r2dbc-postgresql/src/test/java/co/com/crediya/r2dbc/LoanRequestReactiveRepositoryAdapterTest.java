package co.com.crediya.r2dbc;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.r2dbc.adapter.LoanRequestReactiveRepositoryAdapter;
import co.com.crediya.r2dbc.entity.LoanRequestsEntity;
import co.com.crediya.r2dbc.repository.LoanRequestReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class LoanRequestReactiveRepositoryAdapterTest {

    @Mock
    private LoanRequestReactiveRepository loanRequestReactiveRepository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ReactiveTransactionManager transactionManager;

    @Mock
    private ReactiveTransaction transaction;

    @InjectMocks
    private LoanRequestReactiveRepositoryAdapter repositoryAdapter;

    private LoanRequests testLoanRequest;
    private LoanRequestsEntity testLoanRequestEntity;

    @BeforeEach
    void setUp() {
        // Setup test data
        testLoanRequest = LoanRequests.builder()
                .id(1L)
                .amount(1000.0)
                .term(12)
                .email("test@example.com")
                .loanState(LoanState.builder().id(1L).name("PENDING").build())
                .loanType(LoanType.builder().id(1L).name("PERSONAL").build())
                .build();

        testLoanRequestEntity = LoanRequestsEntity.builder()
                .id(1L)
                .amount(1000.0)
                .term(12)
                .email("test@example.com")
                .loanStateId(1L)
                .loanTypeId(1L)
                .build();

        // Setup lenient mocks for transaction manager
        lenient().when(transactionManager.getReactiveTransaction(any()))
                .thenReturn(Mono.just(transaction));
        lenient().when(transactionManager.commit(any()))
                .thenReturn(Mono.empty());
        lenient().when(transactionManager.rollback(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void save_ShouldReturnSavedLoanRequest() {
        // Arrange
        when(loanRequestReactiveRepository.save(any(LoanRequestsEntity.class)))
                .thenReturn(Mono.just(testLoanRequestEntity));
        when(loanRequestReactiveRepository.findById(anyLong()))
                .thenReturn(Mono.just(testLoanRequestEntity));

        // Act & Assert
        StepVerifier.create(repositoryAdapter.save(testLoanRequest))
                .expectNextMatches(savedRequest ->
                        savedRequest.getId().equals(1L) &&
                                savedRequest.getEmail().equals("test@example.com")
                )
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnLoanRequest_WhenFound() {
        // Arrange
        when(loanRequestReactiveRepository.findById(1L))
                .thenReturn(Mono.just(testLoanRequestEntity));


        // Act & Assert
        StepVerifier.create(repositoryAdapter.findById(1L))
                .expectNextMatches(request ->
                        request.getId().equals(1L) &&
                                request.getEmail().equals("test@example.com")
                )
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        // Arrange
        when(loanRequestReactiveRepository.findById(999L))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(repositoryAdapter.findById(999L))
                .verifyComplete();
    }

    @Test
    void save_ShouldHandleError() {
        // Arrange
        when(loanRequestReactiveRepository.save(any(LoanRequestsEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(repositoryAdapter.save(testLoanRequest))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findByLoanStatesId_ShouldReturnFilteredLoans() {
        // Arrange
        List<Long> stateIds = Arrays.asList(1L, 2L, 3L);
        int page = 0;
        int size = 10;

        when(loanRequestReactiveRepository.findByLoanStateIdIn(eq(stateIds), any(Pageable.class)))
                .thenReturn(Flux.just(testLoanRequestEntity));

        // Act & Assert
        StepVerifier.create(repositoryAdapter.findByLoanStatesId(stateIds, page, size))
                .expectNextMatches(loan ->
                        loan.getId().equals(1L) &&
                                loan.getEmail().equals("test@example.com")
                )
                .verifyComplete();
    }

    @Test
    void findByLoanStatesId_ShouldReturnEmpty_WhenNoMatches() {
        // Arrange
        List<Long> stateIds = Arrays.asList(4L, 5L, 6L);
        int page = 0;
        int size = 10;

        when(loanRequestReactiveRepository.findByLoanStateIdIn(eq(stateIds), any(Pageable.class)))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(repositoryAdapter.findByLoanStatesId(stateIds, page, size))
                .verifyComplete();
    }
}