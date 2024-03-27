package car.sharing.service;

import car.sharing.dto.UserRegistrationRequestDto;
import car.sharing.dto.UserResponseDto;
import car.sharing.dto.UserSearchParametersDto;
import car.sharing.dto.UserUpdateInfoRequestDto;
import car.sharing.dto.UserUpdatedRolesResponseDto;
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
