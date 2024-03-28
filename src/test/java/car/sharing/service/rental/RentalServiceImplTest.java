package car.sharing.service.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.exception.CarRentalException;
import car.sharing.mapper.RentalMapper;
import car.sharing.model.Car;
import car.sharing.model.Payment;
import car.sharing.model.Rental;
import car.sharing.model.User;
import car.sharing.repository.CarRepository;
import car.sharing.repository.PaymentRepository;
import car.sharing.repository.RentalRepository;
import car.sharing.telegram.strategy.NotificationStrategy;
import car.sharing.telegram.strategy.rental.TelegramRentalReturningNotificationService;
import com.stripe.exception.StripeException;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private CarRepository carRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationStrategy<Rental> notificationStrategy;
    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Verify that addRental() works as expected with valid params")
    void addRental_ValidParams_ReturnValidResponse() {
        User user = new User();
        user.setId(1L);

        Car car = createCar("Test brand", "Test model");
        car.setId(1L);

        int daysToRent = 5;

        Rental rental = createRental(Rental.Status.PENDING, car.getId(), user.getId(), daysToRent);

        RentalResponseDto expected = createResponseDto(rental);

        when(rentalRepository.findActiveByUserId(user.getId())).thenReturn(Optional.empty());
        when(paymentRepository.findByTypeAndUserIdAndStatus(
                Payment.Type.FINE,
                user.getId(),
                Payment.Status.PENDING))
                .thenReturn(Optional.empty());
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(rentalRepository.save(any())).thenReturn(rental);
        when(carRepository.save(any())).thenReturn(car);
        when(rentalMapper.toResponseDto(any())).thenReturn(expected);

        RentalResponseDto actual = rentalService.addRental(user, car.getId(), daysToRent);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that addRental() works as expected with already added rental")
    void addRental_RentalIsAlreadyCreated_ThrowsException() {
        User user = new User();
        user.setId(1L);

        when(rentalRepository.findActiveByUserId(user.getId()))
                .thenReturn(Optional.of(new Rental()));

        CarRentalException exception = assertThrows(CarRentalException.class, () ->
                rentalService.addRental(user, 1L, 5));

        String expected = "You already have a pending or lasting rental,"
                + " pay for it first or cancel it."
                + " After that you can rent another car";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that addRental() works as expected when user has a fine")
    void addRental_FineIsNotPaid_ThrowsException() {
        User user = new User();
        user.setId(1L);

        Payment payment = createFinePayment();

        when(rentalRepository.findActiveByUserId(user.getId())).thenReturn(Optional.empty());
        when(paymentRepository.findByTypeAndUserIdAndStatus(
                Payment.Type.FINE,
                user.getId(),
                Payment.Status.PENDING))
                .thenReturn(Optional.of(payment));

        CarRentalException exception = assertThrows(CarRentalException.class,
                () -> rentalService.addRental(user, 1L, 5));

        String expected = "You can't rent a new car until you pay fine";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that addRental() works as expected with non-valid car id")
    void addRental_InvalidCarId_ThrowsException() {
        User user = new User();
        user.setId(1L);

        when(rentalRepository.findActiveByUserId(user.getId())).thenReturn(Optional.empty());
        when(paymentRepository.findByTypeAndUserIdAndStatus(
                any(),
                any(),
                any()))
                .thenReturn(Optional.empty());

        when(carRepository.findById(anyLong())).thenReturn(Optional.empty());

        long carId = anyLong();

        EntityNotFoundException notFoundException = assertThrows(EntityNotFoundException.class,
                () -> rentalService.addRental(user, carId, 5));

        String expected = "There is no car by id " + carId;
        String actual = notFoundException.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that addRental() works as expected with when car is not available")
    void addRental_InvalidCarInventory_ThrowsException() {
        User user = new User();
        user.setId(1L);

        Car car = createCar("Test brand", "Test model");
        car.setInventory(0);

        when(rentalRepository.findActiveByUserId(user.getId())).thenReturn(Optional.empty());
        when(paymentRepository.findByTypeAndUserIdAndStatus(
                any(),
                any(),
                any()))
                .thenReturn(Optional.empty());

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));

        CarRentalException noInventoryException = assertThrows(CarRentalException.class,
                () -> rentalService.addRental(user, car.getId(), 5));

        String expected = "Sorry, this car is not available now. Every one is busy";
        String actual = noInventoryException.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that getUserRentals() works as expected with valid params")
    void getUserRentals_ActiveRentals_() {
        Rental rental = createRental(
                Rental.Status.LASTING,
                1L,
                1L,
                5);

        RentalResponseDto expected = createResponseDto(rental);

        when(rentalRepository.findRentalByStatusAndUserId(
                Rental.Status.LASTING,
                rental.getUserId()))
                .thenReturn(Optional.of(rental));
        when(rentalMapper.toResponseDto(rental)).thenReturn(expected);

        List<RentalResponseDto> expectedList = List.of(expected);

        List<RentalResponseDto> actualList = rentalService.getUserRentals(
                rental.getUserId(),
                true,
                PageRequest.of(0, 5));

        assertEquals(expectedList, actualList);
        assertEquals(expected, actualList.get(0));

        verify(rentalMapper, times(1)).toResponseDto(any());
        verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify that getUserRentals() works as expected with valid params")
    void getUserRentals_NotActiveRentals_() {
        Rental firstRental = createRental(
                Rental.Status.RETURNED,
                1L,
                1L,
                5);

        Rental secondRental = createRental(
                Rental.Status.RETURNED,
                1L,
                1L,
                5);

        RentalResponseDto firstDto = createResponseDto(firstRental);
        RentalResponseDto secondDto = createResponseDto(secondRental);

        List<Rental> rentals = List.of(firstRental, secondRental);

        when(rentalRepository.findAllWhereActualReturnDateIsNotNull(
                any(),
                any())).thenReturn(rentals);
        when(rentalMapper.toResponseDto(firstRental)).thenReturn(firstDto);
        when(rentalMapper.toResponseDto(secondRental)).thenReturn(secondDto);

        List<RentalResponseDto> expected = List.of(firstDto, secondDto);

        List<RentalResponseDto> actual = rentalService.getUserRentals(
                1L,
                false,
                PageRequest.of(0, 5));

        assertEquals(expected, actual);

        verify(rentalMapper, times(2)).toResponseDto(any());
        verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify that getSpecificRental() works as expected with valid params")
    void getSpecificRental_ValidParam_ReturnsValidResponse() {
        Rental rental = createRental(Rental.Status.LASTING, 1L, 1L, 5);
        rental.setId(1L);

        RentalResponseDto expected = createResponseDto(rental);

        when(rentalRepository.findById(anyLong())).thenReturn(Optional.of(rental));
        when(rentalMapper.toResponseDto(rental)).thenReturn(expected);

        RentalResponseDto actual = rentalService.getSpecificRental(1L);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName(
            "Verify that getSpecificRental() throws an exception when passing non-valid rental id")
    void getSpecificRental_NonValidParam_ThrowsException() {
        Long rentalId = 1251261363L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.getSpecificRental(rentalId));

        String expected = "There is no rental by id " + rentalId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that getAllRentals() method works as expected with valid params")
    void getAllRentals_ValidParams_ReturnsValidResponse() {
        Rental firstRental = createRental(
                Rental.Status.RETURNED,
                1L,
                1L,
                5);

        Rental secondRental = createRental(
                Rental.Status.RETURNED,
                1L,
                1L,
                5);

        User user = new User();
        user.setId(1L);

        RentalResponseDto firstDto = createResponseDto(firstRental);
        RentalResponseDto secondDto = createResponseDto(secondRental);

        List<Rental> rentals = List.of(firstRental, secondRental);

        when(rentalRepository.findAllByUserId(
                user.getId(),
                PageRequest.of(0, 5)))
                .thenReturn(rentals);
        when(rentalMapper.toResponseDto(firstRental)).thenReturn(firstDto);
        when(rentalMapper.toResponseDto(secondRental)).thenReturn(secondDto);

        List<RentalResponseDto> expected = List.of(firstDto, secondDto);

        List<RentalResponseDto> actual =
                rentalService.getAllRentals(user, PageRequest.of(0, 5));

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that setReturnDate() method works as expected with valid params")
    void setReturnDate_ValidParams_ReturnsValidResponse()
            throws StripeException, MalformedURLException {
        User user = new User();
        user.setId(1L);

        Car car = createCar("Test brand", "Test model");
        car.setId(1L);

        Rental rental =
                createRental(Rental.Status.LASTING, car.getId(), user.getId(), 5);
        rental.setActualReturnDate(LocalDate.now());

        RentalResponseDto expected = createResponseDto(rental);
        expected.setActualReturnDate(LocalDate.now().toString());

        when(rentalRepository.findRentalByStatusAndUserId(
                Rental.Status.LASTING,
                user.getId()))
                .thenReturn(Optional.of(rental));
        when(carRepository.findById(rental.getCarId())).thenReturn(Optional.of(car));
        when(notificationStrategy.getNotificationService(any(), any()))
                .thenReturn(mock(TelegramRentalReturningNotificationService.class));
        when(rentalMapper.toResponseDto(rental)).thenReturn(expected);

        RentalResponseDto actual = rentalService.setReturnDate(user);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that cancel() method works as expected with valid params")
    void cancel_UserHasRentalAndPayment_DeletesIt() {
        User user = new User();
        user.setId(1L);

        Rental rental = createRental(Rental.Status.PENDING, 1L, user.getId(), 5);

        Payment payment = createPayment();

        when(rentalRepository.findRentalByStatusAndUserId(Rental.Status.LASTING, user.getId()))
                .thenReturn(Optional.empty());
        when(rentalRepository.findRentalByStatusAndUserId(Rental.Status.PENDING, user.getId()))
                .thenReturn(Optional.of(rental));
        when(paymentRepository.findByRentalId(rental.getId())).thenReturn(Optional.of(payment));

        rentalService.cancel(user);

        assertEquals(rental.getStatus(), Rental.Status.CANCELED);
        assertEquals(payment.getStatus(), Payment.Status.CANCELED);
        assertTrue(rental.isDeleted());
        assertTrue(payment.isDeleted());
    }

    @Test
    @DisplayName("Verify that getAllActive() method works as expected with valid params")
    void getAllActive_ValidParams_ReturnsValidResponse() {
        Rental firstRental =
                createRental(Rental.Status.LASTING, 1L, 1L, 5);
        Rental secondRental =
                createRental(Rental.Status.LASTING, 2L, 2L, 10);

        RentalResponseDto firstDto = createResponseDto(firstRental);
        RentalResponseDto secondDto = createResponseDto(secondRental);

        List<Rental> rentals = List.of(firstRental, secondRental);

        when(rentalRepository.findAllByStatus(
                Rental.Status.LASTING,
                PageRequest.of(0, 5)))
                .thenReturn(rentals);
        when(rentalMapper.toResponseDto(firstRental)).thenReturn(firstDto);
        when(rentalMapper.toResponseDto(secondRental)).thenReturn(secondDto);

        List<RentalResponseDto> expected = List.of(firstDto, secondDto);
        List<RentalResponseDto> actual =
                rentalService.getAllActive(PageRequest.of(0, 5));

        assertEquals(expected, actual);
    }

    private Payment createFinePayment() {
        return Payment.builder()
                .rentalId(1L)
                .type(Payment.Type.FINE)
                .status(Payment.Status.PENDING)
                .userId(1L)
                .amountToPay(BigDecimal.TEN)
                .build();
    }

    private Car createCar(String brand, String model) {
        return Car.builder()
                .model(model)
                .type(Car.Type.UNIVERSAL)
                .inventory(10)
                .dailyFee(BigDecimal.TEN)
                .brand(brand)
                .build();
    }

    private Rental createRental(
            Rental.Status status,
            Long carId,
            Long userId,
            int daysToRent) {
        return Rental.builder()
                .status(status)
                .rentalDate(LocalDate.now())
                .carId(carId)
                .userId(userId)
                .requiredReturnDate(LocalDate.now().plusDays(daysToRent))
                .build();
    }

    private RentalResponseDto createResponseDto(Rental rental) {
        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(rental.getId());
        responseDto.setUserId(rental.getUserId());
        responseDto.setCarId(rental.getCarId());
        responseDto.setRentalDate(rental.getRentalDate());
        responseDto.setRequiredReturnDate(rental.getRequiredReturnDate());
        return responseDto;
    }

    private Payment createPayment() {
        return Payment.builder()
                .rentalId(1L)
                .type(Payment.Type.PAYMENT)
                .status(Payment.Status.PENDING)
                .userId(1L)
                .amountToPay(BigDecimal.TEN)
                .build();
    }
}
