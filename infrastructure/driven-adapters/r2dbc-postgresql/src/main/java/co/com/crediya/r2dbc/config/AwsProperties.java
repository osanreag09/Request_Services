package co.com.crediya.r2dbc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
    private String region;
    private SqsConfig sqs;
    private SnsConfig sns;

    @Data
    public static class SqsConfig {
        private String queueUrl;
    }

    @Data
    public static class SnsConfig {
        private String topicArn;
    }
}