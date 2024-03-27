package car.sharing.service.car;

import car.sharing.dto.car.CarResponseDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CarUpdateDto;
import car.sharing.dto.car.CreateCarRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto create(CreateCarRequestDto requestDto);

    List<CarResponseDto> getAllCars(Pageable pageable);

    CarResponseDto getInfo(Long id);

    CarResponseDto update(Long id, CarUpdateDto updateDto);

    void delete(Long id);

    List<CarResponseDto> search(CarSearchParametersDto parametersDto, Pageable pageable);
}
