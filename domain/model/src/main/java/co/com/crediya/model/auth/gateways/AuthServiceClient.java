package co.com.crediya.model.auth.gateways;

import co.com.crediya.model.auth.UserInfo;
import reactor.core.publisher.Mono;

public interface AuthServiceClient {
    Mono<UserInfo> getUserInfo(String email, String token);
}
