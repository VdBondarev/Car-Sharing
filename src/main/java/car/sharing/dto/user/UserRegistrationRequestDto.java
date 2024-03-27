package car.sharing.dto.user;

import car.sharing.annotation.FieldMatch;
import car.sharing.annotation.StartsWithCapital;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

@FieldMatch(password = "password", repeatPassword = "repeatPassword")
public record UserRegistrationRequestDto(
        @NotBlank
        @StartsWithCapital
        String firstName,
        @NotBlank
        @StartsWithCapital
        String lastName,
        @NotBlank
        @Email
        String email,
        @Length(min = 8, max = 35)
        String password,
        @Length(min = 8, max = 35)
        String repeatPassword
) {
}
