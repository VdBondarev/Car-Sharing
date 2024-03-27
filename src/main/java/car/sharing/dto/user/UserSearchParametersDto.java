package car.sharing.dto.user;

public record UserSearchParametersDto(
        String firstName,
        String lastName,
        String email
) {
}
