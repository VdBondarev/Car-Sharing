package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.car.CarResponseDto;
import car.sharing.dto.car.CarUpdateDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.model.Car;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarResponseDto toResponseDto(Car car);

    @Mapping(target = "type", ignore = true)
    Car toModel(CreateCarRequestDto requestDto);

    @Mapping(target = "type", ignore = true)
    void toModel(@MappingTarget Car car, CarUpdateDto updateDto);

    @AfterMapping
    default void setType(@MappingTarget Car car, CreateCarRequestDto requestDto) {
        Car.Type type = Car.Type.fromString(requestDto.type());
        car.setType(type);
    }

    @AfterMapping
    default void setType(@MappingTarget Car car, CarUpdateDto updateDto) {
        if (updateDto.type() != null) {
            Car.Type type = Car.Type.fromString(updateDto.type());
            car.setType(type);
        }
    }
}
