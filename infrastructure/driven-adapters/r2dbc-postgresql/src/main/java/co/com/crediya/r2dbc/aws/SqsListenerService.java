package co.com.crediya.r2dbc.aws;

/*
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsListenerService {
    private final SqsClient sqsClient;
    private final SnsPublisherService snsPublisherService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String queueUrl = "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/my-queue";

    public void pollMessages() {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(5)
                .waitTimeSeconds(10)
                .messageAttributeNames("All")
                .attributeNamesWithStrings("All")
                .build();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                sqsClient.receiveMessage(receiveRequest).messages().forEach(this::processMessage);
            } catch (Exception e) {
                log.error("Error recibiendo mensajes de SQS", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processMessage(Message message) {
        try {
            log.info("📩 Mensaje recibido: {}", message.body());

            // Parsear el mensaje SNS
            JsonNode snsMessage = objectMapper.readTree(message.body());

            // Verificar si es una notificación SNS
            if (snsMessage.has("Type") && "Notification".equals(snsMessage.get("Type").asText())) {
                // Extraer los atributos del mensaje
                JsonNode messageAttributes = snsMessage.path("MessageAttributes");

                // Extraer valores de los atributos
                String estado = messageAttributes.path("estado").path("Value").asText();
                String email = messageAttributes.path("email").path("Value").asText();
                Long solicitudId = messageAttributes.path("solicitudId").path("Value").asLong();

                // Extraer el mensaje principal
                String mensajeTexto = snsMessage.path("Message").asText();

                log.info("📋 Detalles del mensaje:");
                log.info("  - ID Solicitud: {}", solicitudId);
                log.info("  - Estado: {}", estado);
                log.info("  - Email: {}", email);
                log.info("  - Mensaje: {}", mensajeTexto);

                // Aquí iría la lógica de negocio para procesar el mensaje
                // Por ejemplo, actualizar el estado en la base de datos
                // updateLoanStatus(solicitudId, estado, email);

                // Eliminar el mensaje de la cola
                deleteMessageFromQueue(message.receiptHandle());
            } else {
                log.warn("⚠️ El mensaje no es una notificación SNS válida");
            }
        } catch (Exception e) {
            log.error("❌ Error procesando mensaje: {}", e.getMessage(), e);
        }
    }

    private void deleteMessageFromQueue(String receiptHandle) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();

            sqsClient.deleteMessage(deleteRequest);
            log.info("✅ Mensaje eliminado correctamente de la cola");
        } catch (Exception e) {
            log.error("❌ Error eliminando mensaje de la cola: {}", e.getMessage(), e);
        }
    }
}
 */