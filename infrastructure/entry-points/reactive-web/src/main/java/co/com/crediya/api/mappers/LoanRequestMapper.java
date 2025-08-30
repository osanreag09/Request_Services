package co.com.crediya.api.mappers;

import co.com.crediya.api.dtos.LoanRequestsDTO;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requests.LoanRequests;
import co.com.crediya.model.states.LoanState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoanRequestMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static LoanRequestsDTO toDTO(LoanRequests loanRequests) {
        return LoanRequestsDTO.builder()
            .id(loanRequests.getId())
            .amount(loanRequests.getAmount())
            .term(loanRequests.getTerm())
            .email(loanRequests.getEmail())
            .loanType(loanRequests.getLoanType().getId().toString())
            .loanState(loanRequests.getLoanState().getId().toString())
            .build();
    }

    public static LoanRequests toDomain(LoanRequestsDTO loanRequestsDTO) {
        logObjects(loanRequestsDTO);
        LoanRequests loanRequests = LoanRequests.builder()
                .id(loanRequestsDTO.getId())
                .amount(loanRequestsDTO.getAmount())
                .term(loanRequestsDTO.getTerm())
                .email(loanRequestsDTO.getEmail())
                .loanType(LoanType.builder().name(loanRequestsDTO.getLoanType()).build())
                .loanState(LoanState.builder().name(loanRequestsDTO.getLoanState()).build())
                .build();

        logObjects(loanRequests);
        return loanRequests;
    }

    private static void logObjects(Object object) {
        try {
            log.info("Converting DTO TO MODEL: {}",
                    objectMapper.writeValueAsString(object));
        } catch (Exception e){
            log.error("Error logging LoanRequestsDTO to LoanRequests: {}", object.toString());
        }
    }
}
