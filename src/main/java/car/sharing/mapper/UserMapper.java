package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.UserRegistrationRequestDto;
import car.sharing.dto.UserResponseDto;
import car.sharing.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseDto toResponseDto(User user);
}
