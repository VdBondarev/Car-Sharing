package car.sharing.controller;

import car.sharing.dto.UserLoginRequestDto;
import car.sharing.dto.UserLoginResponseDto;
import car.sharing.dto.UserRegistrationRequestDto;
import car.sharing.dto.UserResponseDto;
import car.sharing.exception.RegistrationException;
import car.sharing.security.AuthenticationService;
import car.sharing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication controller", description = "Endpoints for authentication")
@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    @Operation(summary = "Registration for any user", description = "Endpoint for registration")
    public UserResponseDto register(
            @RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @PostMapping("/login")
    @Operation(summary = "Login for signed-up users", description = "Endpoint for login")
    public UserLoginResponseDto login(
            @RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}
