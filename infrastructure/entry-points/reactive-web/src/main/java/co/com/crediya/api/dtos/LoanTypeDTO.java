package co.com.crediya.api.dtos;

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
public class LoanTypeDTO {
    private String name;
    private double minimumAmount;
    private double maximumAmount;
    private double interestRate;
    private boolean automaticValidation;
}
