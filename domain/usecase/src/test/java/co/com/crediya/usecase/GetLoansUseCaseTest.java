package co.com.crediya.usecase;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.model.states.gateways.StatesRepository;
import co.com.crediya.usecase.requestloan.GetLoansUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLoansUseCaseTest {

    @Mock
    private RequestsRepository requestsRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private StatesRepository loanStateRepository;

    @InjectMocks
    private GetLoansUseCase getLoansUseCase;

    private LoanRequests testLoan;

    @BeforeEach
    void setUp() {
        testLoan = createTestLoanRequest();
    }

    @Test
    void testExecute_ShouldReturnLoans() {
        // Given
        List<Long> expectedStateIds = List.of(1L, 3L, 6L);
        when(requestsRepository.findByLoanStatesId(expectedStateIds, 0, 10))
                .thenReturn(Flux.just(testLoan));
        when(loanTypeRepository.findById(testLoan.getLoanType().getId()))
                .thenReturn(Mono.just(testLoan.getLoanType()));
        when(loanStateRepository.findById(testLoan.getLoanState().getId()))
                .thenReturn(Mono.just(testLoan.getLoanState()));

        // When
        Flux<LoanRequests> result = getLoansUseCase.execute(0, 10);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(loan ->
                        loan.getId().equals(1L) &&
                                loan.getAmount() == 1000.0 &&
                                loan.getTerm() == 12
                )
                .verifyComplete();
    }

    @Test
    void testExecute_WithPagination_ShouldReturnPaginatedLoans() {
        // Given
        List<Long> expectedStateIds = List.of(1L, 3L, 6L);
        when(requestsRepository.findByLoanStatesId(expectedStateIds, 20, 10))
                .thenReturn(Flux.just(testLoan));
        when(loanTypeRepository.findById(testLoan.getLoanType().getId()))
                .thenReturn(Mono.just(testLoan.getLoanType()));
        when(loanStateRepository.findById(testLoan.getLoanState().getId()))
                .thenReturn(Mono.just(testLoan.getLoanState()));

        // When
        Flux<LoanRequests> result = getLoansUseCase.execute(20, 10);

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testExecute_NoLoansFound_ShouldReturnEmpty() {
        // Given
        List<Long> expectedStateIds = List.of(1L, 3L, 6L);
        when(requestsRepository.findByLoanStatesId(expectedStateIds, 0, 10))
                .thenReturn(Flux.empty());

        // When
        Flux<LoanRequests> result = getLoansUseCase.execute(0, 10);

        // Then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    private LoanRequests createTestLoanRequest() {
        return LoanRequests.builder()
                .id(1L)
                .amount(1000.0)
                .term(12)
                .email("test@example.com")
                .loanType(LoanType.builder().id(1L).name("HOME").build())
                .loanState(LoanState.builder().id(1L).name("PENDING").build())
                .build();
    }
}