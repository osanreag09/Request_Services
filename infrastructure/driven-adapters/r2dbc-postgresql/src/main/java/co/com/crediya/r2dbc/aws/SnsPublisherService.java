package co.com.crediya.r2dbc.aws;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;
/*
@Service
public class SnsPublisherService {

    private final SnsClient snsClient;
    private final String topicArn = "arn:aws:sns:us-east-1:000000000000:my-topic"; //TODO: Cambiar por el que esta en yml.

    public SnsPublisherService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public Mono<String> publishNotification(Long solicitudId, String estado, String emailDestino) {
        return Mono.fromCallable(() -> {
            String mensaje = "La solicitud " + solicitudId + " fue " + estado;

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(mensaje)
                    .subject("Estado de tu solicitud")
                    .messageAttributes(Map.of(
                            "solicitudId", MessageAttributeValue.builder()
                                    .dataType("Number")
                                    .stringValue(solicitudId.toString())
                                    .build(),
                            "email", MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(emailDestino)
                                    .build(),
                            "estado", MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(estado)
                                    .build()
                    ))
                    .build();

            PublishResponse response = snsClient.publish(request);
            return response.messageId();
        });
    }
}
 */