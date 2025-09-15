package co.com.crediya.r2dbc.aws;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class SimpleLocalContext implements Context {
    private final String functionName;
    public SimpleLocalContext(String functionName) {
        this.functionName = functionName;
    }
    @Override public String getAwsRequestId() { return "local-request"; }
    @Override public String getLogGroupName() { return "local-log-group"; }
    @Override public String getLogStreamName() { return "local-log-stream"; }
    @Override public String getFunctionName() { return functionName; }
    @Override public String getFunctionVersion() { return "local-1"; }
    @Override public String getInvokedFunctionArn() { return "arn:aws:lambda:local:000000000000:function:"+functionName; }
    @Override public CognitoIdentity getIdentity() { return null; }
    @Override public ClientContext getClientContext() { return null; }
    @Override public int getRemainingTimeInMillis() { return 300000; }
    @Override public int getMemoryLimitInMB() { return 512; }
    @Override public LambdaLogger getLogger() {
        return new LambdaLogger() {
            @Override public void log(String s) { System.out.println("[LAMBDA] " + s); }
            @Override public void log(byte[] bytes) { System.out.println("[LAMBDA] " + new String(bytes)); }
        };
    }
}