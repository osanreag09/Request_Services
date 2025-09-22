package co.com.crediya.api;

import co.com.crediya.api.config.TestSecurityConfig;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.usecase.requestloan.UpdateLoansUseCase;
import co.com.crediya.usecase.requestloan.gateways.GetLoans;
import co.com.crediya.usecase.requestloan.gateways.RegistryRequestLoan;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
@Import(TestSecurityConfig.class)
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private RegistryRequestLoan registryRequestLoan;

    @MockitoBean
    private GetLoans getLoans;

    @MockitoBean
    private UpdateLoansUseCase updateLoansUseCase;

    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        when(registryRequestLoan.execute(any(LoanRequests.class), anyString()))
                .thenReturn(Mono.just(LoanRequests.builder()
                        .id(1L)
                        .amount(1000.0)
                        .term(12)
                        .email(TEST_EMAIL)
                        .loanType(LoanType.builder().id(1L).name("HOME").build())
                        .loanState(LoanState.builder().id(1L).name("PENDING").build())
                        .build()));
    }

    @Test
    @WithMockUser
    void testCreateLoanRequest_ShouldReturnOk() {
        webTestClient.post()
                .uri("/api/v1/solicitud")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "amount": 1000.0,
                        "term": 12,
                        "clientId": "12345",
                        "clientName": "Test User",
                        "clientEmail": "test@example.com",
                        "clientPhone": "1234567890",
                        "loanType": "HOME",
                        "loanState": "PENDING"
                    }
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @WithMockUser
    void testUpdateRequest_ShouldReturnUpdatedLoan() {
        // Given
        LoanRequests testLoan = createTestLoanRequest();
        LoanRequests updatedLoan = testLoan.toBuilder()
                .loanState(LoanState.builder().id(2L).name("APPROVED").build())
                .build();

        when(updateLoansUseCase.execute(any(LoanRequests.class)))
                .thenReturn(Mono.just(updatedLoan));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "id": 1,
                        "amount": 1000.0,
                        "term": 12,
                        "email": "test@example.com",
                        "loanType": "HOME",
                        "loanState": "APPROVED"
                    }
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(updatedLoan.getId().intValue())
                .jsonPath("$.amount").isEqualTo(updatedLoan.getAmount())
                .jsonPath("$.term").isEqualTo(updatedLoan.getTerm())
                .jsonPath("$.email").isEqualTo(updatedLoan.getEmail())
                .jsonPath("$.loanType").isEqualTo(updatedLoan.getLoanType().getName())
                .jsonPath("$.loanState").isEqualTo("APPROVED");

        // Verify that the updateLoansUseCase.execute was called with the correct parameters
        verify(updateLoansUseCase).execute(argThat(loan -> 
            loan.getId().equals(1L) && 
            loan.getLoanState().getName().equals("APPROVED")
        ));
    }

    @Test
    @WithMockUser
    void testUpdateRequest_WhenLoanNotFound_ShouldReturnNotFound() {
        // Given
        when(updateLoansUseCase.execute(any(LoanRequests.class)))
                .thenReturn(Mono.error(new RuntimeException("Loan not found")));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "id": 999,
                        "amount": 1000.0,
                        "term": 12,
                        "email": "test@example.com",
                        "loanType": "HOME",
                        "loanState": "APPROVED"
                    }
                    """)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @WithMockUser
    void testGetLoanRequests_ShouldReturnLoanList() {
        // Given
        LoanRequests testLoan = createTestLoanRequest();
        when(getLoans.execute(0, 10))
                .thenReturn(Flux.just(testLoan));

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/solicitud")
                        .queryParam("init", "0")
                        .queryParam("size", "10")
                        .build())
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(testLoan.getId().intValue())
                .jsonPath("$[0].amount").isEqualTo(testLoan.getAmount())
                .jsonPath("$[0].term").isEqualTo(testLoan.getTerm())
                .jsonPath("$[0].email").isEqualTo(testLoan.getEmail())
                .jsonPath("$[0].loanType").isEqualTo(testLoan.getLoanType().getName())
                .jsonPath("$[0].loanState").isEqualTo(testLoan.getLoanState().getName())
                .jsonPath("$[0].interestRate").isEqualTo(testLoan.getLoanType().getInterestRate());
    }

    private LoanRequests createTestLoanRequest() {
        return LoanRequests.builder()
                .id(1L)
                .amount(1000.0)
                .term(12)
                .email(TEST_EMAIL)
                .loanType(LoanType.builder().id(1L).name("HOME").build())
                .loanState(LoanState.builder().id(1L).name("PENDING").build())
                .build();
    }
}
