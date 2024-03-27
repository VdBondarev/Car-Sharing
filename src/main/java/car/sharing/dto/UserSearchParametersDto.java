package car.sharing.dto;

public record UserSearchParametersDto(
        String firstName,
        String lastName,
        String email
) {
}
