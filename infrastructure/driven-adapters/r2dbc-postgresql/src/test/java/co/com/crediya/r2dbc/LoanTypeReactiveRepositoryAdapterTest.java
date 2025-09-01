package co.com.crediya.r2dbc;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.r2dbc.adapter.LoanTypeReactiveRepositoryAdapter;
import co.com.crediya.r2dbc.entity.LoanTypeEntity;
import co.com.crediya.r2dbc.repository.LoanTypeReactiveRepository;
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
class LoanTypeReactiveRepositoryAdapterTest {

    @Mock
    private LoanTypeReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ReactiveTransactionManager transactionManager;

    @InjectMocks
    private LoanTypeReactiveRepositoryAdapter adapter;

    private LoanType testLoanType;
    private LoanTypeEntity testLoanTypeEntity;

    @BeforeEach
    void setUp() {
        testLoanType = LoanType.builder()
                .id(1L)
                .name("PERSONAL")
                .minimumAmount(1000.0)
                .maximumAmount(50000.0)
                .interestRate(12.5)
                .automaticValidation(true)
                .build();

        testLoanTypeEntity = LoanTypeEntity.builder()
                .id(1L)
                .name("PERSONAL")
                .minimumAmount(1000.0)
                .maximumAmount(50000.0)
                .interestRate(12.5)
                .automaticValidation(true)
                .build();
    }

    @Test
    void findById_ShouldReturnLoanType_WhenFound() {
        // Arrange
        when(repository.findById(any(Long.class)))
                .thenReturn(Mono.just(testLoanTypeEntity));
        when(mapper.map(any(LoanTypeEntity.class), any(Class.class)))
                .thenReturn(testLoanType);

        // Act & Assert
        StepVerifier.create(adapter.findById(1L))
                .expectNextMatches(loanType ->
                        loanType.getId().equals(1L) &&
                                loanType.getName().equals("PERSONAL") &&
                                loanType.getMinimumAmount() == 1000.0 &&
                                loanType.getMaximumAmount() == 50000.0 &&
                                loanType.getInterestRate() == 12.5 &&
                                loanType.isAutomaticValidation()
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

    @Test
    void findByName_ShouldReturnLoanType_WhenFound() {
        // Arrange
        when(repository.findByName(anyString()))
                .thenReturn(Mono.just(testLoanTypeEntity));
        when(mapper.map(any(LoanTypeEntity.class), any(Class.class)))
                .thenReturn(testLoanType);

        // Act & Assert
        StepVerifier.create(adapter.findByName("PERSONAL"))
                .expectNextMatches(loanType ->
                        loanType.getId().equals(1L) &&
                                loanType.getName().equals("PERSONAL") &&
                                loanType.getMinimumAmount() == 1000.0
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
    void existsByName_ShouldReturnTrue_WhenNameExists() {
        // Arrange
        when(repository.existsByName(anyString()))
                .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(adapter.existsByName("PERSONAL"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNameDoesNotExist() {
        // Arrange
        when(repository.existsByName(anyString()))
                .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(adapter.existsByName("NON_EXISTENT"))
                .expectNext(false)
                .verifyComplete();
    }
}