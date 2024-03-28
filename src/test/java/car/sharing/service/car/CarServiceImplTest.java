package car.sharing.service.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import car.sharing.dto.car.CarResponseDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CarUpdateDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.mapper.CarMapper;
import car.sharing.model.Car;
import car.sharing.repository.CarRepository;
import car.sharing.repository.specification.car.CarSpecificationBuilder;
import car.sharing.telegram.strategy.NotificationStrategy;
import car.sharing.telegram.strategy.car.TelegramCarCreationNotificationService;
import car.sharing.telegram.strategy.car.TelegramCarUpdatingNotificationService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {
    private static final String CAR_CREATION = "car creation";
    private static final String TELEGRAM = "telegram";
    private static final String CAR_UPDATING = "car updating";
    private static final String MODEL_COLUMN = "model";
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;
    @Mock
    private CarSpecificationBuilder carSpecificationBuilder;
    @Mock
    private NotificationStrategy<Car> notificationStrategy;
    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Verify that create() method works as expected with valid params")
    void create_ValidCar_ReturnsValidResponse() {
        CreateCarRequestDto requestDto = new CreateCarRequestDto(
                "Test model",
                "Test brand",
                "Universal",
                10,
                BigDecimal.valueOf(10.99)
        );

        Car car = Car.builder()
                .model(requestDto.model())
                .type(Car.Type.UNIVERSAL)
                .inventory(requestDto.inventory())
                .dailyFee(requestDto.dailyFee())
                .brand(requestDto.brand())
                .id(1L)
                .build();

        CarResponseDto expected = new CarResponseDto(
                car.getId(),
                car.getModel(),
                car.getBrand(),
                car.getType(),
                car.getInventory(),
                car.getDailyFee()
        );

        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toResponseDto(car)).thenReturn(expected);
        when(notificationStrategy.getNotificationService(TELEGRAM, CAR_CREATION))
                .thenReturn(mock(TelegramCarCreationNotificationService.class));

        CarResponseDto actual = carService.create(requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that getAllCars() method works as expected with valid params")
    void getAllCars_ValidParams_ReturnsValidResponses() {
        Car firstCar = createCar(1L, "Test brand 1", "Test model 1");
        Car secondCar = createCar(2L, "Test brand 2", "Test model 2");

        Pageable pageable = PageRequest.of(0, 5);
        List<Car> cars = List.of(firstCar, secondCar);

        CarResponseDto firstDto = createResponseDto(firstCar);
        CarResponseDto secondDto = createResponseDto(secondCar);

        when(carRepository.findAllAvailable(pageable)).thenReturn(cars);
        when(carMapper.toResponseDto(firstCar)).thenReturn(firstDto);
        when(carMapper.toResponseDto(secondCar)).thenReturn(secondDto);

        List<CarResponseDto> expected = List.of(firstDto, secondDto);
        List<CarResponseDto> actual = carService.getAllCars(pageable);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0), actual.get(0));
        assertEquals(expected, actual);

        pageable = PageRequest.of(1, 5);

        expected = List.of();
        actual = carService.getAllCars(pageable);

        assertEquals(expected.size(), actual.size());

        verify(carRepository, times(2)).findAllAvailable(any());
        verify(carMapper, times(2)).toResponseDto(any());
        verifyNoMoreInteractions(carRepository);
        verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("Verify that getInfo() method works as expected with valid params")
    void getInfo_ValidParam_ReturnsValidResponse() {
        Car car = createCar(1L, "Test brand", "Test model");

        Optional<Car> carOptional = Optional.of(car);

        CarResponseDto expected = createResponseDto(car);

        when(carRepository.findById(car.getId())).thenReturn(carOptional);
        when(carMapper.toResponseDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.getInfo(car.getId());

        assertEquals(expected, actual);

        verify(carRepository, times(1)).findById(any());
        verify(carMapper, times(1)).toResponseDto(any());
        verifyNoMoreInteractions(carRepository);
        verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("Verify that findById() method works as expected with non-valid params")
    void findById_NonValidId_ThrowsException() {
        when(carRepository.findById(any())).thenReturn(Optional.empty());

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> carService.getInfo(1L));

        String expected = "There is no car available by id 1";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that update() method works as expected with valid params")
    void update_ValidParams_ReturnsValidResponse() {
        Car car = createCar(1L, "Test brand", "Test model");

        Optional<Car> carOptional = Optional.of(car);

        CarUpdateDto updateDto =
                createUpdateDto(car,"Changed test brand", "Changed test model");

        CarResponseDto expected = new CarResponseDto(
                car.getId(),
                updateDto.model(),
                updateDto.brand(),
                car.getType(),
                car.getInventory(),
                car.getDailyFee());

        when(carRepository.findById(car.getId())).thenReturn(carOptional);
        when(notificationStrategy.getNotificationService(
                TELEGRAM, CAR_UPDATING))
                .thenReturn(mock(TelegramCarUpdatingNotificationService.class));
        doAnswer(invocation -> {
            Car carToUpdate = invocation.getArgument(0);
            CarUpdateDto dtoToUpdate = invocation.getArgument(1);
            carToUpdate.setBrand(dtoToUpdate.brand());
            carToUpdate.setModel(dtoToUpdate.model());
            return null;
        }).when(carMapper).toModel(car, updateDto);

        when(carMapper.toResponseDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.update(1L, updateDto);

        assertEquals(expected, actual);

        verify(carRepository, times(1)).findById(any());
        verify(carMapper, times(1)).toModel(any(), any());
        verify(carRepository, times(1)).save(any());
        verify(carMapper, times(1)).toResponseDto(any());

        verifyNoMoreInteractions(carRepository);
        verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("Verify that getInfo() method works as expected with non-valid params")
    void getInfo_NonValidId_ThrowsException() {
        Long nonValidId = 1095544L;

        when(carRepository.findById(nonValidId)).thenReturn(Optional.empty());

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class,
                        () -> carService.getInfo(nonValidId));

        String expected = "There is no car available by id " + nonValidId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that search() method works as expected with valid params")
    void search_ValidParams_ReturnsValidResponse() {
        Car car = createCar(1L, "Test brand", "Test model");

        CarSearchParametersDto parametersDto =
                createSearchParams("Test model");

        Specification<Car> specification = (root, query, criteriaBuilder)
                -> root.get(MODEL_COLUMN).in(parametersDto.models());

        CarResponseDto expectedDto = createResponseDto(car);
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Car> page = new PageImpl<>(
                List.of(car),
                pageable,
                List.of(car).size());

        when(carSpecificationBuilder.build(parametersDto)).thenReturn(specification);
        when(carRepository.findAll(specification, pageable))
                .thenReturn(page);
        when(carMapper.toResponseDto(car)).thenReturn(expectedDto);

        List<CarResponseDto> expectedList = List.of(expectedDto);
        List<CarResponseDto> actualList = carService.search(
                parametersDto, pageable);

        assertEquals(expectedList, actualList);
        assertEquals(expectedDto, actualList.get(0));
        assertEquals(expectedList.size(), actualList.size());

        verify(carRepository, times(1)).findAll(specification, pageable);
        verify(carSpecificationBuilder, times(1)).build(parametersDto);
        verifyNoMoreInteractions(carRepository);
        verifyNoMoreInteractions(carSpecificationBuilder);
    }

    @Test
    @DisplayName("Verify that search() method works as expected with non-valid params")
    void search_EmptyRequest_ThrowsException() {

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> carService.search(
                                new CarSearchParametersDto(
                                        null,
                                        null,
                                        null,
                                        null),
                                PageRequest.of(0, 10)));

        String expected = "Searching should be done by at least one param, but was 0";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    private CarSearchParametersDto createSearchParams(String model) {
        return new CarSearchParametersDto(
                List.of(model), List.of(),
                        null,
                        null);
    }

    private CarUpdateDto createUpdateDto(Car car, String brand, String model) {
        return new CarUpdateDto(
                model,
                brand,
                car.getInventory(),
                car.getDailyFee(),
                car.getType().name());
    }

    private CarResponseDto createResponseDto(Car car) {
        return new CarResponseDto(
                car.getId(),
                car.getModel(),
                car.getBrand(),
                car.getType(),
                car.getInventory(),
                car.getDailyFee());
    }

    private Car createCar(Long id, String brand, String model) {
        Car car = Car.builder()
                .brand(brand)
                .dailyFee(BigDecimal.TEN)
                .inventory(10)
                .type(Car.Type.UNIVERSAL)
                .model(model)
                .id(id)
                .build();
        return car;
    }
}
