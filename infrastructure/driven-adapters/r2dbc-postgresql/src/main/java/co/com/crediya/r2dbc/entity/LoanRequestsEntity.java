package co.com.crediya.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("loan_requests")
public class LoanRequestsEntity {
    private Long id;
    private Double amount;
    private int  term;
    private String email;
    private Long loanTypeId;
    private Long loanStateId;
}
