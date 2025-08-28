package co.com.crediya.model.states.gateways;

import co.com.crediya.model.states.LoanState;
import reactor.core.publisher.Mono;

public interface StatesRepository {
    Mono<LoanState> findByName(String name);
    Mono<LoanState> findById(Long id);
}
