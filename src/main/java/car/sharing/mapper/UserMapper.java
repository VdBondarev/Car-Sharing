package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.UserRegistrationRequestDto;
import car.sharing.dto.UserResponseDto;
import car.sharing.dto.UserUpdateInfoRequestDto;
import car.sharing.dto.UserUpdatedRolesResponseDto;
import car.sharing.model.Role;
import car.sharing.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseDto toResponseDto(User user);

    @Mapping(target = "rolesIds", ignore = true)
    UserUpdatedRolesResponseDto toUpdatedResponseDto(User user);

    @Mapping(target = "password", ignore = true)
    void updateModel(@MappingTarget User user, UserUpdateInfoRequestDto requestDto);

    @AfterMapping
    default void setRolesIds(
            @MappingTarget UserUpdatedRolesResponseDto responseDto,
            User user) {
        Set<Long> rolesIds = user.getRoles()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        responseDto.setRolesIds(rolesIds);
    }
}
