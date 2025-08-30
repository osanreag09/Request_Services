package co.com.crediya.r2dbc;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.requests.gateways.RequestsRepository;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.r2dbc.entity.LoanRequestsEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import co.com.crediya.r2dbc.mapper.LoanRequestsDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

@Slf4j
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

    @Override
    public Mono<LoanRequests> save(LoanRequests loanRequests) {
        // The ID is now handled in the mapper
        LoanRequestsEntity entity = LoanRequestsDataMapper.toEntity(loanRequests);
        
        return repository.save(entity)
                .flatMap(savedEntity -> findById(savedEntity.getId()))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<LoanRequests> findById(Long id) {
        return repository.findById(id)
                .map(entity ->
                        LoanRequests.builder()
                                .id(entity.getId())
                                .amount(entity.getAmount())
                                .term(entity.getTerm())
                                .email(entity.getEmail())
                                .loanType(LoanType.builder()
                                        .id(entity.getLoanTypeId())
                                        .build())
                                .loanState(LoanState.builder()
                                        .id(entity.getLoanStateId())
                                        .build())
                                .build());
    }
}
