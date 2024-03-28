package car.sharing.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserSearchParametersDto;
import car.sharing.dto.user.UserUpdatedRolesResponseDto;
import car.sharing.exception.RegistrationException;
import car.sharing.mapper.UserMapper;
import car.sharing.model.Role;
import car.sharing.model.User;
import car.sharing.repository.UserRepository;
import car.sharing.repository.specification.user.UserSpecificationBuilder;
import car.sharing.telegram.strategy.NotificationStrategy;
import car.sharing.telegram.strategy.user.TelegramRoleUpdatingNotificationService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserSpecificationBuilder userSpecificationBuilder;
    @Mock
    private NotificationStrategy<User> notificationStrategy;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Verify that registration works fine for valid input params")
    void register_ValidRequest_RegistersUser() throws RegistrationException {
        UserRegistrationRequestDto requestDto =
                createRegistrationRequestDto("test@gmail.com", "testPassword");

        User user = createUser(requestDto);

        UserResponseDto expected = createResponseDto(user);

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.password())).thenReturn(requestDto.password());
        when(userMapper.toResponseDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.register(requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that registration works as expected for already registered email")
    void register_AlreadyRegisteredEmail_ThrowsException() {
        UserRegistrationRequestDto requestDto =
                createRegistrationRequestDto("test@gmail.com", "testPassword");

        User user = createUser(requestDto);

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(user));

        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));

        String expected = "User with passed email is already registered, try another one.";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that updateUserRole() works fine when updating customer to customer")
    void updateUserRole_AlreadyCustomer_ReturnsNothingUpdated() {
        User user = createUser();

        UserUpdatedRolesResponseDto expected = createUpdatedDto(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toUpdatedResponseDto(user)).thenReturn(expected);

        UserUpdatedRolesResponseDto actual =
                userService.updateUserRole(user.getId(), "ROLE_CUSTOMER");

        // verify that user has only customer role after updating (nothing should be updated)
        assertEquals(Set.of(1L), actual.getRolesIds());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that updateUserRole() works fine when updating manager to customer")
    void updateUserRole_UpdateManagerToCustomer_ReturnsUpdatedUser() {
        Role customer = new Role(1L);
        customer.setName(Role.RoleName.ROLE_CUSTOMER);

        Role manager = new Role(2L);
        customer.setName(Role.RoleName.ROLE_MANAGER);

        // now this user is a manager
        User user = createUser();
        user.setRoles(Set.of(customer, manager));

        UserUpdatedRolesResponseDto expected = createUpdatedDto(user);
        expected.setRolesIds(Set.of(1L));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(notificationStrategy.getNotificationService(any(), any()))
                .thenReturn(mock(TelegramRoleUpdatingNotificationService.class));
        when(userMapper.toUpdatedResponseDto(user)).thenReturn(expected);

        UserUpdatedRolesResponseDto actual =
                userService.updateUserRole(user.getId(), "ROLE_CUSTOMER");

        assertEquals(expected, actual);
        assertEquals(1, actual.getRolesIds().size());
    }

    @Test
    @DisplayName("Verify that updateUserRole() works fine when updating customer to manager")
    void updateUserRole_UpdateCustomerToManager_ReturnsUpdatedUser() {
        User user = createUser();

        // expecting that user will be manager after updating
        UserUpdatedRolesResponseDto expected = createUpdatedDto(user);
        expected.setRolesIds(Set.of(1L, 2L));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(notificationStrategy.getNotificationService(any(), any()))
                .thenReturn(mock(TelegramRoleUpdatingNotificationService.class));
        when(userMapper.toUpdatedResponseDto(user)).thenReturn(expected);

        UserUpdatedRolesResponseDto actual =
                userService.updateUserRole(user.getId(), "ROLE_MANAGER");

        assertEquals(expected, actual);
        assertEquals(2, actual.getRolesIds().size());
    }

    @Test
    @DisplayName("Verify that search() works fine with valid params")
    void search_ValidParams_ReturnsValidResponse() {
        User user = createUser();

        UserSearchParametersDto parametersDto = new UserSearchParametersDto(
                "test",
                "test",
                null
        );

        Specification<User> firstNameSpec = (root, query, criteriaBuilder)
                -> criteriaBuilder.like(root.get("firstName"), parametersDto.firstName());

        Specification<User> lastNameSpec = ((root, query, criteriaBuilder)
                -> criteriaBuilder.like(root.get("lastName"), parametersDto.lastName()));

        Specification<User> specification = firstNameSpec.and(lastNameSpec);

        PageRequest pageable = PageRequest.of(0, 5);
        Page<User> page = new PageImpl<>(
                List.of(user),
                pageable,
                List.of(user).size());

        UserResponseDto expectedDto = createResponseDto(user);

        when(userSpecificationBuilder.build(parametersDto)).thenReturn(specification);
        when(userRepository.findAll(specification, pageable)).thenReturn(page);
        when(userMapper.toResponseDto(user)).thenReturn(expectedDto);

        List<UserResponseDto> expectedList = List.of(expectedDto);
        List<UserResponseDto> actualList = userService.search(parametersDto, pageable);

        assertEquals(expectedList.get(0), actualList.get(0));
        assertEquals(expectedList, actualList);
    }

    @Test
    @DisplayName("Verify that search() throws an exception when passing non-valid params")
    void search_NullPassedParams_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.search(null, PageRequest.of(0, 5)));

        String expected = "Searching should be done by at least one param, but was 0";
        String actual = exception.getMessage();

        assertEquals(expected, actual);

        exception = assertThrows(IllegalArgumentException.class, () -> userService.search(
                new UserSearchParametersDto(
                        null,
                        null,
                        null
                ), PageRequest.of(0, 5)));

        actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    private UserUpdatedRolesResponseDto createUpdatedDto(User user) {
        UserUpdatedRolesResponseDto updatedRolesResponseDto =
                new UserUpdatedRolesResponseDto();
        updatedRolesResponseDto.setRolesIds(user
                .getRoles()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toSet()));
        updatedRolesResponseDto.setEmail(user.getEmail());
        updatedRolesResponseDto.setFirstName(user.getFirstName());
        updatedRolesResponseDto.setLastName(user.getLastName());
        updatedRolesResponseDto.setId(user.getId());
        return updatedRolesResponseDto;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("testemail@example.com");
        user.setPassword("testpassword");
        user.setLastName("Test");
        user.setFirstName("Test");
        return user;
    }

    private User createUser(UserRegistrationRequestDto requestDto) {
        User user = new User();
        user.setId(1L);
        user.setPassword(requestDto.password());
        user.setRoles(Set.of(new Role(1L)));
        user.setEmail(requestDto.email());
        user.setLastName(requestDto.lastName());
        user.setFirstName(requestDto.firstName());
        return user;
    }

    private UserResponseDto createResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }

    private UserRegistrationRequestDto createRegistrationRequestDto(String email, String password) {
        return new UserRegistrationRequestDto(
                "Firstname",
                "LastName",
                email,
                password,
                password);
    }
}
