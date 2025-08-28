package co.com.crediya.r2dbc.mapper;

import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.r2dbc.entity.LoanRequestsEntity;

public class LoanRequestsDataMapper {
    public static LoanRequests toDomain(LoanRequestsEntity entity) {
        return LoanRequests.builder()
                .amount(entity.getAmount())
                .term(entity.getTerm())
                .email(entity.getEmail())
                //TODO: Ver como se hace el mapeo en esta parte.

                .build();
    }

    public static LoanRequestsEntity toEntity(LoanRequests loanRequests) {
        return LoanRequestsEntity.builder()
                .amount(loanRequests.getAmount())
                .term(loanRequests.getTerm())
                .email(loanRequests.getEmail())
                .loanStateId(loanRequests.getLoanState().getId())
                .loanTypeId(loanRequests.getLoanType().getId())
                .build();
    }
}
