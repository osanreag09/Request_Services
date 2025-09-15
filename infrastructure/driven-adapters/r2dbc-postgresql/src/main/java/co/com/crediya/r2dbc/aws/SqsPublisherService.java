package co.com.crediya.r2dbc.aws;

import co.com.crediya.r2dbc.aws.evendto.NotificationEvent;
import co.com.crediya.r2dbc.config.AwsConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsPublisherService {

    private final SqsClient sqsClient;
    private final AwsConfig awsConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<String> send(NotificationEvent event) {
        return Mono.fromCallable(() -> {
            String queueUrl = awsConfig.getSqs().getQueueUrl();
            if (queueUrl == null || queueUrl.isBlank()) {
                throw new IllegalStateException("aws.sqs.queue-url no configurado");
            }

            String body = toJson(event);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(request);
            log.info("Mensaje enviado a SQS. messageId={}, queue={}", response.messageId(), queueUrl);
            return response.messageId();
        });
    }

    private String toJson(NotificationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando NotificationEvent", e);
        }
    }
}