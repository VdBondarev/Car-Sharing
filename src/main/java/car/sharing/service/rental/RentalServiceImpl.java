package car.sharing.service.rental;

import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.exception.CarRentalException;
import car.sharing.mapper.RentalMapper;
import car.sharing.model.Car;
import car.sharing.model.Rental;
import car.sharing.model.User;
import car.sharing.repository.CarRepository;
import car.sharing.repository.RentalRepository;
import car.sharing.telegram.strategy.NotificationStrategy;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private static final int ONE = 1;
    private static final String TELEGRAM = "telegram";
    private static final String RENTAL_RETURNING = "rental returning";
    private static final int NO_INVENTORY = 0;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final NotificationStrategy<Rental> notificationStrategy;

    @Override
    public RentalResponseDto addRental(
            User user,
            Long carId,
            int daysToRent) {
        checkIfRentalExists(user);
        Car car = ifAvailable(carId);
        Rental rental = createRental(user, daysToRent, carId);
        rentalRepository.save(rental);
        car.setInventory(car.getInventory() - ONE);
        carRepository.save(car);
        return rentalMapper.toResponseDto(rental);
    }

    @Override
    public List<RentalResponseDto> getUserRentals(
            Long userId,
            boolean isActive,
            Pageable pageable) {
        return isActive
                ? mapToResponseDto(
                        List.of(
                                rentalRepository.findRentalByStatusAndUserId(
                                        Rental.Status.LASTING,
                                                userId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "User doesn't have an active rental"))))
                : mapToResponseDto(
                rentalRepository.findAllWhereActualReturnDateIsNotNull(userId, pageable));
    }

    @Override
    public RentalResponseDto getSpecificRental(Long id) {
        return rentalRepository.findById(id)
                .map(rentalMapper::toResponseDto)
                .orElseThrow(
                    () -> new EntityNotFoundException("There is no rental by id " + id));
    }

    @Override
    public List<RentalResponseDto> getAllRentals(User user, Pageable pageable) {
        return mapToResponseDto(
                rentalRepository.findAllByUserId(
                        user.getId(),
                        pageable));
    }

    @Override
    public RentalResponseDto setReturnDate(User user) {
        Rental rental = rentalRepository.findRentalByStatusAndUserId(
                        Rental.Status.LASTING,
                        user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "You don't have an active rental yet"));
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find car by id " + rental.getCarId()));
        rental.setActualReturnDate(LocalDate.now());
        car.setInventory(car.getInventory() + ONE);
        rental.setStatus(Rental.Status.RETURNED);
        carRepository.save(car);
        rentalRepository.save(rental);
        sendMessage(TELEGRAM, RENTAL_RETURNING, rental, null);
        return rentalMapper.toResponseDto(rental);
    }

    @Override
    public void cancel(User user) {
        rentalRepository
                .findRentalByStatusAndUserId(
                        Rental.Status.PENDING,
                        user.getId())
                .ifPresent(rental -> {
                    rental.setDeleted(true);
                    rental.setStatus(Rental.Status.CANCELED);
                    rentalRepository.save(rental);
                    carRepository.findById(rental.getCarId()).ifPresent(car -> {
                        car.setInventory(car.getInventory() + ONE);
                        carRepository.save(car);
                    });
                });
    }

    @Override
    public List<RentalResponseDto> getAllActive(Pageable pageable) {
        return rentalRepository.findAllByStatus(Rental.Status.LASTING, pageable)
                .stream()
                .map(rentalMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private List<RentalResponseDto> mapToResponseDto(List<Rental> rentals) {
        return rentals
                .stream()
                .map(rentalMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private void checkIfRentalExists(User user) {
        if (rentalRepository.findActiveByUserId(user.getId())
                .isPresent()) {
            throw new CarRentalException(
                    "You already have a pending or lasting rental, pay for it first or cancel it."
                            + " After that you can rent another car");
        }
    }

    private Car ifAvailable(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("There is no car by id " + carId));
        if (car.getInventory() <= NO_INVENTORY) {
            throw new CarRentalException("Sorry, this car is not available now."
                    + " Every one is busy");
        }
        return car;
    }

    private Rental createRental(User user, int daysToRent, Long carId) {
        return new Rental.Builder()
                .setRentalDate(LocalDate.now())
                .setUserId(user.getId())
                .setRequiredReturnDate(LocalDate.now().plusDays(daysToRent))
                .setCarId(carId)
                .setStatus(Rental.Status.PENDING)
                .build();
    }

    private void sendMessage(
            String notificationService,
            String messageType,
            Rental rental,
            Long chatId) {
        notificationStrategy.getNotificationService(
                notificationService, messageType
                )
                .sendMessage(rental, chatId);
    }
}
