package co.com.crediya.r2dbc.aws;

/*
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component

public class SqsListenerStarter {
    private final SqsListenerService sqsListenerService;

    public SqsListenerStarter(SqsListenerService sqsListenerService) {
        this.sqsListenerService = sqsListenerService;
    }

    @PostConstruct
    public void init() {
        new Thread(sqsListenerService::pollMessages, "sqs-listener")
                .start();
    }
}
 */