package co.com.crediya.api.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    @Test
    void validate_ShouldReturnValidDto_WhenValidationPasses() {
        // Arrange
        TestDto validDto = new TestDto("test", 10);

        // Act & Assert
        StepVerifier.create(ValidationUtil.validate(validDto, validator))
                .expectNext(validDto)
                .verifyComplete();
    }

    @Test
    void validate_ShouldReturnError_WhenDtoIsNull() {
        // Act & Assert
        StepVerifier.create(ValidationUtil.validate(null, validator))
                .verifyErrorSatisfies(throwable -> {
                    assertThat(throwable)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessageContaining("Request body cannot be null");
                    assertThat(((ResponseStatusException) throwable).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void validate_ShouldReturnError_WhenValidationFails() {
        // Arrange
        TestDto invalidDto = new TestDto("", -1);

        // Act & Assert
        StepVerifier.create(ValidationUtil.validate(invalidDto, validator))
                .verifyErrorSatisfies(throwable -> {
                    assertThat(throwable)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessageContaining("name must not be blank")
                            .hasMessageContaining("value must be positive");
                    assertThat(((ResponseStatusException) throwable).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    // Removed Lombok annotations and made it a simple record
    private record TestDto(
            @NotBlank(message = "name must not be blank")
            String name,
            @NotNull @Positive(message = "value must be positive")
            Integer value
    ) {}
}