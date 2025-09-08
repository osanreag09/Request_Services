package co.com.crediya.r2dbc.repository;

import co.com.crediya.r2dbc.entity.LoanRequestsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanRequestReactiveRepository extends ReactiveCrudRepository<LoanRequestsEntity, Long>, ReactiveQueryByExampleExecutor<LoanRequestsEntity> {

    Mono<LoanRequestsEntity> save(LoanRequestsEntity loanRequestsEntity);

    Mono<LoanRequestsEntity> findById(Long id);

    Flux<LoanRequestsEntity> findAll();

    @Query("SELECT lr.* FROM loan_requests lr " +
            "WHERE lr.loan_state_id IN (:loanStateIds) " +
            "ORDER BY lr.id " +
            "LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}")
    Flux<LoanRequestsEntity> findByLoanStateIdIn(
            @Param("loanStateIds") List<Long> loanStateIds,
            @Param("pageable") Pageable pageable
    );
}
