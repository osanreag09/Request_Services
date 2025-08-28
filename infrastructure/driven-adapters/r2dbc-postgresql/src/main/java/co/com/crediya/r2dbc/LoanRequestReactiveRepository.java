package co.com.crediya.r2dbc;

import co.com.crediya.r2dbc.entity.LoanRequestsEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanRequestReactiveRepository extends ReactiveCrudRepository<LoanRequestsEntity, Long>, ReactiveQueryByExampleExecutor<LoanRequestsEntity> {

}
