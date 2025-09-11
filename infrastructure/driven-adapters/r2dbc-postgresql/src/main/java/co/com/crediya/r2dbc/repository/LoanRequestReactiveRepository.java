package co.com.crediya.r2dbc.repository;

import co.com.crediya.r2dbc.entity.LoanRequestsEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanRequestReactiveRepository extends ReactiveCrudRepository<LoanRequestsEntity, Long>, ReactiveQueryByExampleExecutor<LoanRequestsEntity> {

    Mono<LoanRequestsEntity> save(LoanRequestsEntity loanRequestsEntity);

    Mono<LoanRequestsEntity> findById(Long id);
}
