package co.com.crediya.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanRequestsDTO {
    private Long id;

    @NotNull(message = "Amount is required")
    private Double amount;
    private int  term;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must have a valid domain")
    private String email;

    @NotBlank(message = "Loan type is required")
    private String loanType;
    private String loanState;
}
