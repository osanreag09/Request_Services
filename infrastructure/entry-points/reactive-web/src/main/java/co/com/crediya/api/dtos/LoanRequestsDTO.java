package co.com.crediya.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Amount of the loan", example = "1000.0")
    private Double amount;

    @Schema(description = "Term of the loan in months", example = "12")
    private int  term;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must have a valid domain")
    @Schema(description = "Email of the user", example = "user@example.com")
    private String email;

    @NotBlank(message = "Loan type is required")
    @Schema(description = "Type of the loan", example = "PERSONAL")
    private String loanType;

    @Schema(description = "State of the loan", example = "PENDING")
    private String loanState;

    @Schema(description = "Interest rate of the loan", example = "0.05")
    private double interestRate;
}
