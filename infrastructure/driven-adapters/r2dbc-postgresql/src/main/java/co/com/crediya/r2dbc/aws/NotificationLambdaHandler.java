package co.com.crediya.r2dbc.aws;

import co.com.crediya.r2dbc.aws.evendto.NotificationEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Slf4j
public class NotificationLambdaHandler implements RequestHandler<SQSEvent, String> {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private String topicArn = System.getenv("aws.sns.topic-arn");

    public NotificationLambdaHandler() {
        this.snsClient = SnsClient.builder()
                .region(Region.of(System.getenv("aws.region")))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public NotificationLambdaHandler(SnsClient snsClient, ObjectMapper objectMapper, String topicArn) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.topicArn = topicArn;
    }

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        event.getRecords().forEach(msg -> {
            try {
                context.getLogger().log("Procesando mensaje SQS: " + msg.getBody());
                NotificationEvent ne = objectMapper.readValue(msg.getBody(), NotificationEvent.class);

                String subject = "Actualización de tu solicitud #" + ne.getSolicitudId();
                String messageBody = String.format(
                        "Hola,%n%nEl estado de tu solicitud #%s ha sido actualizado a: %s%n%nDetalles:%n%s%n%nGracias por usar nuestro servicio.",
                        ne.getSolicitudId(), ne.getEstado(), ne.getMensaje()
                );

                PublishRequest request = PublishRequest.builder()
                        .topicArn(topicArn)
                        .subject(subject)
                        .message(messageBody)
                        .build();

                PublishResponse response = snsClient.publish(request);
                log.info("Notificación SNS enviada. messageId=" + response.messageId());

            } catch (Exception e) {
                log.info("Error procesando mensaje SQS: " + e.getMessage());
                throw new RuntimeException("Error procesando mensaje", e);
            }
        });
        return "OK";
    }
}