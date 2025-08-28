package co.com.crediya.model.requests;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.states.LoanState;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
@Builder(toBuilder = true)
public class LoanRequests {
    private Long id;
    private Double amount;
    private int term;
    private String email;
    private LoanType loanType;
    private LoanState loanState;
}
