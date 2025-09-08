package co.com.crediya.api;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.usecase.requestloan.gateways.GetLoans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import co.com.crediya.usecase.requestloan.gateways.RegistryRequestLoan;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private RegistryRequestLoan registryRequestLoan;

    @MockitoBean
    private GetLoans getLoans;

    @BeforeEach
    void setUp() {
        when(registryRequestLoan.execute(any(LoanRequests.class), any(String.class)))
                .thenReturn(Mono.just(LoanRequests.builder()
                        .id(1L)
                        .amount(1000.0)
                        .term(12)
                        .email("test@example.com")
                        .loanType(LoanType.builder().id(1L).name("HOME").build())
                        .loanState(LoanState.builder().id(1L).name("PENDING").build())
                        .build()));
    }

    @Test
    void testListenPOSTUseCase() {
        webTestClient.post()
                .uri("/api/v1/solicitud")
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
                .expectStatus().isOk();
    }
}
