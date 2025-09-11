package co.com.crediya.r2dbc.mapper;

import co.com.crediya.model.states.LoanState;
import co.com.crediya.r2dbc.entity.LoanStateEntity;

public class LoanStateDataMapper {
    public static LoanStateEntity toEntity(LoanState loanState) {
        return LoanStateEntity.builder()
                .id(loanState.getId())
                .name(loanState.getName())
                .description(loanState.getDescription())
                .build();
    }

    public static LoanState toDomain(LoanStateEntity loanStateEntity) {
        return LoanState.builder()
                .id(loanStateEntity.getId())
                .name(loanStateEntity.getName())
                .description(loanStateEntity.getDescription())
                .build();
    }
}
