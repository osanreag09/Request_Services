package co.com.crediya.r2dbc.adapter;

import co.com.crediya.r2dbc.aws.SqsPublisherService;
import co.com.crediya.r2dbc.aws.evendto.NotificationEvent;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceAdapterTest {

    @Mock
    private SqsPublisherService sqsPublisherService;

    @InjectMocks
    private NotificationServiceAdapter notificationServiceAdapter;

    @Captor
    private ArgumentCaptor<NotificationEvent> eventCaptor;

    private final Long testRequestId = 123L;
    private final String testStatus = "APPROVED";
    private final String testEmail = "test@example.com";
    private final String expectedMessageId = "test-message-id";

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(sqsPublisherService);
    }

    @Test
    void sendNotification_ShouldSendCorrectEventToSqs() {
        // Arrange
        when(sqsPublisherService.send(any(NotificationEvent.class)))
                .thenReturn(Mono.just(expectedMessageId));

        // Act
        var result = notificationServiceAdapter.sendNotification(testRequestId, testStatus, testEmail);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedMessageId)
                .verifyComplete();

        verify(sqsPublisherService).send(eventCaptor.capture());
        NotificationEvent capturedEvent = eventCaptor.getValue();

        assertAll(
                () -> assertEquals(String.valueOf(testRequestId), capturedEvent.getSolicitudId()),
                () -> assertEquals(testStatus, capturedEvent.getEstado()),
                () -> assertEquals(testEmail, capturedEvent.getEmail()),
                () -> assertEquals("La solicitud " + testRequestId + " fue " + testStatus, 
                                 capturedEvent.getMensaje())
        );
    }

    @Test
    void sendNotification_WhenSqsFails_ShouldPropagateError() {
        // Arrange
        String errorMessage = "SQS Error";
        when(sqsPublisherService.send(any(NotificationEvent.class)))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        // Act & Assert
        StepVerifier.create(notificationServiceAdapter.sendNotification(testRequestId, testStatus, testEmail))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().equals(errorMessage))
                .verify();

        verify(sqsPublisherService).send(any(NotificationEvent.class));
    }

    @Test
    void sendNotification_WithNullParameters_ShouldHandleGracefully() {
        // Arrange
        when(sqsPublisherService.send(any(NotificationEvent.class)))
                .thenReturn(Mono.just(expectedMessageId));

        // Act & Assert - Test with null status and email
        StepVerifier.create(notificationServiceAdapter.sendNotification(testRequestId, null, null))
                .expectNext(expectedMessageId)
                .verifyComplete();

        verify(sqsPublisherService).send(eventCaptor.capture());
        NotificationEvent capturedEvent = eventCaptor.getValue();

        assertAll(
                () -> assertEquals(String.valueOf(testRequestId), capturedEvent.getSolicitudId()),
                () -> assertNull(capturedEvent.getEstado()),
                () -> assertNull(capturedEvent.getEmail()),
                () -> assertEquals("La solicitud " + testRequestId + " fue null", 
                                 capturedEvent.getMensaje())
        );
    }
}
