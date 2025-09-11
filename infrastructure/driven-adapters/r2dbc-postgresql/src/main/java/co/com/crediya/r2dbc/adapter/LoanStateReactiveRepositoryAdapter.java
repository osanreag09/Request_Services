package co.com.crediya.r2dbc.adapter;

import co.com.crediya.model.states.LoanState;
import co.com.crediya.model.states.gateways.StatesRepository;
import co.com.crediya.r2dbc.entity.LoanStateEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import co.com.crediya.r2dbc.mapper.LoanStateDataMapper;
import co.com.crediya.r2dbc.repository.LoanStateReactiveRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

@Repository
public class LoanStateReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanState,
        LoanStateEntity,
        Long,
        LoanStateReactiveRepository
> implements StatesRepository {
    public LoanStateReactiveRepositoryAdapter(LoanStateReactiveRepository repository, ObjectMapper mapper, ReactiveTransactionManager transactionManager) {
        super(repository, mapper, d -> mapper.map(d, LoanState.class), transactionManager);
    }

    @Override
    public Mono<LoanState> findByName(String name) {
        return repository.findByName(name)
                .map(LoanStateDataMapper::toDomain);
    }
}
