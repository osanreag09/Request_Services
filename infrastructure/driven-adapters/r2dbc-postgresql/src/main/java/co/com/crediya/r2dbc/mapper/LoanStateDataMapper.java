package co.com.crediya.r2dbc.mapper;

import co.com.crediya.model.states.LoanState;
import co.com.crediya.r2dbc.entity.LoanStateEntity;

public class LoanStateDataMapper {
    public static LoanStateEntity toEntity(LoanState loanState) {
        return LoanStateEntity.builder()
                .name(loanState.getName())
                .build();
    }

    public static LoanState toDomain(LoanStateEntity loanStateEntity) {
        return LoanState.builder()
                .name(loanStateEntity.getName())
                .build();
    }
}
