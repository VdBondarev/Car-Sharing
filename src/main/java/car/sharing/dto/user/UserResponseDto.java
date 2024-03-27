package car.sharing.dto.user;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
