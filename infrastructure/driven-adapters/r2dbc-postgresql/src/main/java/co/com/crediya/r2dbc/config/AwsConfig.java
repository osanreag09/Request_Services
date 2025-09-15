package co.com.crediya.r2dbc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Data
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AwsConfig {
    private String region;
    private String endpoint;
    private AwsCredentials credentials;
    private SqsConfig sqs;
    private SnsConfig sns;

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey())))
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey())))
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    @Data
    public static class AwsCredentials {
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class SqsConfig {
        private String queueUrl;
    }

    @Data
    public static class SnsConfig {
        private String topicArn;
    }
}