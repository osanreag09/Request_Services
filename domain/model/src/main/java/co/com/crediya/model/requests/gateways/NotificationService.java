package co.com.crediya.model.requests.gateways;

import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<String> sendNotification(Long requestId, String status, String email);
}
