package car.sharing.service.rental;

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
import car.sharing.service.payment.strategy.PaymentStrategy;
import car.sharing.telegram.strategy.NotificationStrategy;
import car.sharing.util.StripeUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final PaymentStrategy paymentStrategy;
    private final StripeUtil stripeUtil;
    private final PaymentRepository paymentRepository;
    private final NotificationStrategy<Rental> notificationStrategy;

    @Override
    public RentalResponseDto addRental(
            User user,
            Long carId,
            int daysToRent) {
        checkIfRentalExists(user);
        checkIfFineExists(user);
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
    public RentalResponseDto setReturnDate(User user)
            throws StripeException, MalformedURLException {
        Rental rental = rentalRepository.findRentalByStatusAndUserId(
                        Rental.Status.LASTING,
                        user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "You don't have an active rental yet"));
        LocalDate requiredReturnDate = rental.getRequiredReturnDate();
        LocalDate now = LocalDate.now();
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find car by id " + rental.getCarId()));
        rental.setActualReturnDate(now);
        if (requiredReturnDate.isBefore(now)) {
            createFinePayment(requiredReturnDate, now, car, rental, user);
        }
        car.setInventory(car.getInventory() + ONE);
        rental.setStatus(Rental.Status.RETURNED);
        carRepository.save(car);
        rentalRepository.save(rental);
        sendMessage(TELEGRAM, RENTAL_RETURNING, rental, null);
        return rentalMapper.toResponseDto(rental);
    }

    @Override
    public void cancel(User user) {
        if (rentalRepository
                .findRentalByStatusAndUserId(
                        Rental.Status.LASTING,
                        user.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "You can't cancel a lasting rental. You can only return it.");
        }
        rentalRepository
                .findRentalByStatusAndUserId(
                        Rental.Status.PENDING,
                        user.getId())
                .ifPresent(rental -> {
                    rental.setDeleted(true);
                    rental.setStatus(Rental.Status.CANCELED);
                    rentalRepository.save(rental);
                    paymentRepository
                            .findByRentalId(rental.getId())
                            .ifPresent(payment -> {
                                payment.setDeleted(true);
                                payment.setStatus(Payment.Status.CANCELED);
                                paymentRepository.save(payment);
                            });
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

    private void checkIfFineExists(User user) {
        if (paymentRepository.findByTypeAndUserIdAndStatus(
                        Payment.Type.FINE,
                        user.getId(),
                        Payment.Status.PENDING)
                .isPresent()) {
            throw new CarRentalException(
                    "You can't rent a new car until you pay fine");
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
        return Rental.builder()
                .rentalDate(LocalDate.now())
                .userId(user.getId())
                .requiredReturnDate(LocalDate.now().plusDays(daysToRent))
                .carId(carId)
                .status(Rental.Status.PENDING)
                .build();
    }

    private Payment createPayment(
            BigDecimal price,
            Rental rental,
            Session session,
            User user,
            Payment.Type type)
            throws MalformedURLException {
        return Payment.builder()
                .type(type)
                .amountToPay(price)
                .rentalId(rental.getId())
                .sessionId(session.getId())
                .sessionUrl(new URL(session.getUrl()))
                .status(Payment.Status.PENDING)
                .userId(user.getId())
                .build();
    }

    private void createFinePayment(
            LocalDate requiredReturnDate,
            LocalDate now,
            Car car,
            Rental rental,
            User user)
            throws StripeException, MalformedURLException {
        long days = ChronoUnit.DAYS.between(requiredReturnDate, now);
        BigDecimal fine = paymentStrategy.getPaymentService(Payment.Type.FINE)
                .calculateAmount(car.getDailyFee(), days);
        Session session = stripeUtil.createSession(
                fine.longValue(),
                car.getBrand() + " " + car.getModel());
        Payment payment = createPayment(
                fine.multiply(BigDecimal.valueOf(0.01).setScale(2, RoundingMode.HALF_UP)),
                rental, session, user, Payment.Type.FINE);
        paymentRepository.save(payment);
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
