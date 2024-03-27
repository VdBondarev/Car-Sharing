package car.sharing.service.user;

import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserSearchParametersDto;
import car.sharing.dto.user.UserUpdateInfoRequestDto;
import car.sharing.dto.user.UserUpdatedRolesResponseDto;
import car.sharing.exception.RegistrationException;
import car.sharing.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserUpdatedRolesResponseDto updateUserRole(Long id, String roleName);

    UserResponseDto getMyProfileInfo(User user);

    UserResponseDto updateProfile(User user, UserUpdateInfoRequestDto requestDto);

    void delete(Long id);

    List<UserResponseDto> search(UserSearchParametersDto parametersDto, Pageable pageable);
}
