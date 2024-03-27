package car.sharing.dto;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
