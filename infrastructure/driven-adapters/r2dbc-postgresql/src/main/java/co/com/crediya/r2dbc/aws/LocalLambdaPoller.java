package co.com.crediya.r2dbc.aws;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import co.com.crediya.r2dbc.config.AwsConfig;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalLambdaPoller {

    private final SqsClient sqsClient;
    private final SnsClient snsClient;
    private final AwsConfig awsConfig; // para leer queueUrl y topicArn
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void poll() {
        String queueUrl = awsConfig.getSqs().getQueueUrl();
        if (queueUrl == null || queueUrl.isBlank()) {
            log.warn("aws.sqs.queue-url no está configurado. Omite poll.");
            return;
        }

        try {
            ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(1)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(req).messages();
            if (messages.isEmpty()) {
                return;
            }

            log.info("Recibidos {} mensajes desde SQS", messages.size());

            for (Message m : messages) {
                try {
                    SQSEvent.SQSMessage sqsMsg = new SQSEvent.SQSMessage();
                    sqsMsg.setMessageId(m.messageId());
                    sqsMsg.setBody(m.body());
                    SQSEvent event = new SQSEvent();
                    event.setRecords(List.of(sqsMsg));

                    String topicArn = awsConfig.getSns().getTopicArn();
                    NotificationLambdaHandler handler =
                            new NotificationLambdaHandler(snsClient, objectMapper, topicArn);

                    handler.handleRequest(event, new SimpleLocalContext("LocalLambdaPoller"));

                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(m.receiptHandle())
                            .build());

                } catch (Exception ex) {
                    log.error("Error procesando mensaje {}, body={}: {}", m.messageId(), m.body(), ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            log.error("Error en polling de SQS: {}", e.getMessage(), e);
        }
    }
}