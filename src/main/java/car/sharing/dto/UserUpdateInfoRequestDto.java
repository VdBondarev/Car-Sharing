package car.sharing.dto;

import car.sharing.annotation.StartsWithCapital;
import jakarta.validation.constraints.Email;

public record UserUpdateInfoRequestDto(
        @StartsWithCapital
        String firstName,
        @StartsWithCapital
        String lastName,
        String password,
        @Email
        String email
) {
}
