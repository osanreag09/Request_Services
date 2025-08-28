package co.com.crediya.r2dbc.mapper;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.r2dbc.entity.LoanTypeEntity;

public class LoanTypeDataMapper {
    public static LoanType toDomain(LoanTypeEntity loanTypeEntity) {
        return LoanType.builder()
                .name(loanTypeEntity.getName())
                .minimumAmount(loanTypeEntity.getMinimumAmount())
                .maximumAmount(loanTypeEntity.getMaximumAmount())
                .interestRate(loanTypeEntity.getInterestRate())
                .build();
    }
    public static LoanTypeEntity toEntity(LoanType loanType) {
        return LoanTypeEntity.builder()
                .name(loanType.getName())
                .minimumAmount(loanType.getMinimumAmount())
                .maximumAmount(loanType.getMaximumAmount())
                .interestRate(loanType.getInterestRate())
                .build();
    }
}
