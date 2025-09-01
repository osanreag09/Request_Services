package co.com.crediya.r2dbc;

import co.com.crediya.model.states.LoanState;
import co.com.crediya.r2dbc.adapter.LoanStateReactiveRepositoryAdapter;
import co.com.crediya.r2dbc.entity.LoanStateEntity;
import co.com.crediya.r2dbc.repository.LoanStateReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanStateReactiveRepositoryAdapterTest {

    @Mock
    private LoanStateReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ReactiveTransactionManager transactionManager;

    @InjectMocks
    private LoanStateReactiveRepositoryAdapter adapter;

    private LoanState testLoanState;
    private LoanStateEntity testLoanStateEntity;

    @BeforeEach
    void setUp() {
        testLoanState = LoanState.builder()
                .id(1L)
                .name("PENDING")
                .description("Loan request is pending approval")
                .build();

        testLoanStateEntity = LoanStateEntity.builder()
                .id(1L)
                .name("PENDING")
                .description("Loan request is pending approval")
                .build();
    }

    @Test
    void findByName_ShouldReturnLoanState_WhenFound() {
        // Arrange
        when(repository.findByName(anyString()))
                .thenReturn(Mono.just(testLoanStateEntity));

        // Act & Assert
        StepVerifier.create(adapter.findByName("PENDING"))
                .expectNextMatches(state ->
                        state.getId().equals(1L) &&
                                state.getName().equals("PENDING") &&
                                state.getDescription().equals("Loan request is pending approval")
                )
                .verifyComplete();
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNotFound() {
        // Arrange
        when(repository.findByName(anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.findByName("NON_EXISTENT"))
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnLoanState_WhenFound() {
        // Arrange
        when(repository.findById(any(Long.class)))
                .thenReturn(Mono.just(testLoanStateEntity));
        when(mapper.map(any(LoanStateEntity.class), any(Class.class)))
                .thenReturn(testLoanState);

        // Act & Assert
        StepVerifier.create(adapter.findById(1L))
                .expectNextMatches(state ->
                        state.getId().equals(1L) &&
                                state.getName().equals("PENDING") &&
                                state.getDescription().equals("Loan request is pending approval")
                )
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        // Arrange
        when(repository.findById(any(Long.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.findById(999L))
                .verifyComplete();
    }
}
