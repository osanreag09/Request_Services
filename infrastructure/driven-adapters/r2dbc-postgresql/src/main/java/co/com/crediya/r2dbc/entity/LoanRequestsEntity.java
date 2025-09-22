package co.com.crediya.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("loan_requests")
public class LoanRequestsEntity {
    @Id
    private Long id;
    private Double amount;
    private int  term;
    private String email;
    private Long loanTypeId;
    private Long loanStateId;
}
