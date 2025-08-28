package co.com.crediya.r2dbc;

import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.r2dbc.entity.LoanRequestsEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;

@Repository
public class LoanRequestReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanRequests,
        LoanRequestsEntity,
        Long,
        LoanRequestReactiveRepository
> implements RequestsRepository {
    public LoanRequestReactiveRepositoryAdapter(LoanRequestReactiveRepository repository, ObjectMapper mapper, ReactiveTransactionManager transactionManager) {
        super(repository, mapper, d -> mapper.map(d, LoanRequests.class), transactionManager);
    }
}
