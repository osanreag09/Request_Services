package co.com.crediya.r2dbc.repository;

import co.com.crediya.r2dbc.entity.LoanStateEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanStateReactiveRepository extends ReactiveCrudRepository<LoanStateEntity, Long>, ReactiveQueryByExampleExecutor<LoanStateEntity> {

    Mono<LoanStateEntity> findByName(String name);
}
