package co.com.crediya.r2dbc.aws;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public class LambdaInitializer implements RequestHandler<SQSEvent, String> {
    private final NotificationLambdaHandler handler;

    public LambdaInitializer() {
        this.handler = new NotificationLambdaHandler();
    }

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        return handler.handleRequest(event, context);
    }
}