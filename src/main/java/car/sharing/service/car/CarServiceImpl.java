package car.sharing.service.car;

import car.sharing.dto.car.CarResponseDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CarUpdateDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.mapper.CarMapper;
import car.sharing.model.Car;
import car.sharing.repository.CarRepository;
import car.sharing.repository.specification.car.CarSpecificationBuilder;
import car.sharing.telegram.strategy.NotificationStrategy;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private static final String TELEGRAM = "telegram";
    private static final String CAR_CREATION = "car creation";
    private static final String CAR_DELETION = "car deletion";
    private static final String CAR_UPDATING = "car updating";
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarSpecificationBuilder carSpecificationBuilder;
    private final NotificationStrategy<Car> notificationStrategy;

    @Override
    public CarResponseDto create(CreateCarRequestDto requestDto) {
        Car car = carMapper.toModel(requestDto);
        carRepository.save(car);
        sendMessage(TELEGRAM, CAR_CREATION, car, null);
        return carMapper.toResponseDto(car);
    }

    @Override
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carRepository.findAllAvailable(pageable)
                .stream()
                .map(carMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CarResponseDto getInfo(Long id) {
        return carRepository.findById(id)
                .map(carMapper::toResponseDto)
                .orElseThrow(
                    () -> new EntityNotFoundException("There is no car available by id " + id));
    }

    @Override
    public CarResponseDto update(Long id, CarUpdateDto updateDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("There is no car available by id " + id));
        carMapper.toModel(car, updateDto);
        carRepository.save(car);
        sendMessage(TELEGRAM, CAR_UPDATING, car, null);
        return carMapper.toResponseDto(car);
    }

    @Override
    public void delete(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find a car by id " + id));
        carRepository.deleteById(id);
        sendMessage(TELEGRAM, CAR_DELETION, car, null);
    }

    @Override
    public List<CarResponseDto> search(CarSearchParametersDto parametersDto, Pageable pageable) {
        if (isEmpty(parametersDto)) {
            throw new IllegalArgumentException(
                    "Searching should be done by at least one param, but was 0");
        }
        return carRepository.findAll(
                carSpecificationBuilder.build(parametersDto), pageable)
                .stream()
                .map(carMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private boolean isEmpty(CarSearchParametersDto parametersDto) {
        return parametersDto == null
                || (parametersDto.brands() == null || parametersDto.brands().isEmpty())
                && (parametersDto.types() == null)
                && (parametersDto.priceBetween() == null)
                && (parametersDto.models() == null || parametersDto.models().isEmpty());
    }

    private void sendMessage(
            String notificationService,
            String messageType,
            Car car,
            Long chatId) {
        notificationStrategy.getNotificationService(
                        notificationService, messageType
                )
                .sendMessage(car, chatId);
    }
}
