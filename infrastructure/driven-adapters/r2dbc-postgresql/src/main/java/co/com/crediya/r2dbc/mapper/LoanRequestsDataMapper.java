package co.com.crediya.r2dbc.mapper;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.states.LoanState;
import co.com.crediya.r2dbc.entity.LoanRequestsEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoanRequestsDataMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static LoanRequests toDomain(LoanRequestsEntity entity) {
        logObjects(entity);
        return LoanRequests.builder()
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
                .build();
    }

    public static LoanRequestsEntity toEntity(LoanRequests loanRequests) {
        logObjects(loanRequests);
        return LoanRequestsEntity.builder()
                .amount(loanRequests.getAmount())
                .term(loanRequests.getTerm())
                .email(loanRequests.getEmail())
                .loanStateId(loanRequests.getLoanState().getId())
                .loanTypeId(loanRequests.getLoanType().getId())
                .build();
    }

    private static void logObjects(Object object) {
        try {
            log.info("Converting Data TO MODEL: {}",
                    objectMapper.writeValueAsString(object));
        } catch (Exception e){
            log.error("Error logging LoanRequestsDTO to LoanRequests: {}", object.toString());
        }
    }
}
