package car.sharing.service;

import car.sharing.dto.UserRegistrationRequestDto;
import car.sharing.dto.UserResponseDto;
import car.sharing.dto.UserSearchParametersDto;
import car.sharing.dto.UserUpdateInfoRequestDto;
import car.sharing.dto.UserUpdatedRolesResponseDto;
import car.sharing.exception.RegistrationException;
import car.sharing.mapper.UserMapper;
import car.sharing.model.Role;
import car.sharing.model.User;
import car.sharing.repository.UserRepository;
import car.sharing.repository.specification.user.UserSpecificationBuilder;
import car.sharing.telegram.strategy.NotificationStrategy;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String TELEGRAM = "telegram";
    private static final String ROLE_UPDATING = "role updating";
    private static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserSpecificationBuilder userSpecificationBuilder;
    private final NotificationStrategy<User> notificationStrategy;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RegistrationException(
                    "User with passed email is already registered, try another one.");
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserUpdatedRolesResponseDto updateUserRole(Long id, String roleName) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        "User with passed id doesn't exist, id: " + id));
        if (alreadyIs(user, roleName)) {
            return userMapper.toUpdatedResponseDto(user);
        }
        if (hasRole(user, ROLE_MANAGER)
                && roleName.equalsIgnoreCase(ROLE_CUSTOMER)) {
            user.setRoles(Set.of(new Role(1L)));
        } else {
            user.setRoles(Set.of(new Role(1L), new Role(2L)));
        }
        userRepository.save(user);
        sendMessage(TELEGRAM, ROLE_UPDATING, user, null);
        return userMapper.toUpdatedResponseDto(user);
    }

    @Override
    public UserResponseDto getMyProfileInfo(User user) {
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto updateProfile(User user, UserUpdateInfoRequestDto requestDto) {
        userMapper.updateModel(user, requestDto);
        if (requestDto.password() != null) {
            user.setPassword(passwordEncoder.encode(requestDto.password()));
        }
        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponseDto> search(UserSearchParametersDto parametersDto, Pageable pageable) {
        if (isEmpty(parametersDto)) {
            throw new IllegalArgumentException(
                    "Searching should be done by at least one param, but was 0");
        }
        return userRepository.findAll(
                        userSpecificationBuilder.build(parametersDto), pageable)
                .stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private boolean isEmpty(UserSearchParametersDto parametersDto) {
        return (parametersDto == null)
                || (parametersDto.firstName() == null || parametersDto.firstName().isEmpty())
                && (parametersDto.lastName() == null || parametersDto.lastName().isEmpty())
                && (parametersDto.email() == null || parametersDto.email().isEmpty());
    }

    private void sendMessage(
            String notificationService,
            String messageType,
            User user,
            Long chatId) {
        notificationStrategy.getNotificationService(
                        notificationService, messageType
                )
                .sendMessage(user, chatId);
    }

    private boolean alreadyIs(User user, String roleName) {
        return (isCustomer(user)
                && roleName.equalsIgnoreCase(ROLE_CUSTOMER))
                || (hasRole(user, ROLE_MANAGER)
                && roleName.equalsIgnoreCase(ROLE_MANAGER));
    }

    private boolean isCustomer(User user) {
        return user.getRoles().size() == 1;
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .toList()
                .contains(Role.RoleName.fromString(roleName));
    }
}
