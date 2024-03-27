package car.sharing.controller;

import car.sharing.dto.UserResponseDto;
import car.sharing.dto.UserSearchParametersDto;
import car.sharing.dto.UserUpdateInfoRequestDto;
import car.sharing.dto.UserUpdatedRolesResponseDto;
import car.sharing.model.User;
import car.sharing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users controller", description = "Endpoints for managing users")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get your profile's info",
            description = "Endpoint for getting your profile's info")
    public UserResponseDto getMyProfileInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getMyProfileInfo(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Update your profile's info",
            description = "Endpoint for updating your profile's info")
    public UserResponseDto updateProfile(
            Authentication authentication,
            @RequestBody UserUpdateInfoRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updateProfile(user, requestDto);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update a user's role",
            description = "Endpoint for updating the user's role."
                    + " Allowed for managers only")
    public UserUpdatedRolesResponseDto updateUserRole(
            @PathVariable Long id,
            @RequestParam(name = "role_name") String roleName) {
        return userService.updateUserRole(id, roleName);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Find users",
            description = "Endpoint for finding users by params. Allowed for managers only")
    public List<UserResponseDto> search(
            @RequestBody @Valid UserSearchParametersDto parametersDto,
            Pageable pageable) {
        return userService.search(parametersDto, pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user",
            description = "Endpoint for deleting the user"
                    + " Allowed for managers only")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
