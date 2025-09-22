package co.com.crediya.r2dbc.adapter;

import co.com.crediya.model.requests.gateways.NotificationService;
import co.com.crediya.r2dbc.aws.SqsPublisherService;
import co.com.crediya.r2dbc.aws.evendto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class NotificationServiceAdapter implements NotificationService {

    private final SqsPublisherService sqsPublisherService;

    public NotificationServiceAdapter(SqsPublisherService sqsPublisherService) {
        this.sqsPublisherService = sqsPublisherService;
    }

    @Override
    public Mono<String> sendNotification(Long requestId, String status, String email) {
        log.info("Encolando notificación en SQS. RequestId: {}, Status: {}, Email: {}", requestId, status, email);
        NotificationEvent event = new NotificationEvent(
                String.valueOf(requestId),
                status,
                email,
                "La solicitud " + requestId + " fue " + status
        );
        return sqsPublisherService.send(event);
    }
}
