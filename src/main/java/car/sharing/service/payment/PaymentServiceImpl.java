package car.sharing.service.payment;

import car.sharing.dto.payment.PaymentResponseDto;
import car.sharing.mapper.PaymentMapper;
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
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final String TELEGRAM = "telegram";
    private static final String SUCCESSFUL_PAYMENT = "successful payment";
    private static final String RENTAL_CREATION = "rental creation";
    private static final int ONE = 1;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final StripeUtil stripeUtil;
    private final PaymentStrategy paymentStrategy;
    private final NotificationStrategy<Payment> paymentNotificationStrategy;
    private final NotificationStrategy<Rental> rentalNotificationStrategy;
    @Value("${stripe.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    @Override
    public List<PaymentResponseDto> getUserPayments(Long userId, Pageable pageable) {
        return paymentRepository.findAllByUserId(userId, pageable)
                .stream()
                .map(paymentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDto create(User user)
            throws StripeException, MalformedURLException {
        if (paymentRepository.findByStatusAndUserId(
                Payment.Status.PENDING,
                user.getId())
                .isPresent()) {
            throw new IllegalArgumentException("You already have a pending payment."
                    + " You should pay for that first or cancel your rental");
        }
        Rental rental = rentalRepository.findRentalByStatusAndUserId(
                        Rental.Status.PENDING,
                        user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "You should create a rental first. Then you can pay for that."));
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find a car by id " + rental.getCarId()));
        long days = ChronoUnit.DAYS.between(
                rental.getRentalDate(),
                rental.getRequiredReturnDate());
        BigDecimal price = paymentStrategy.getPaymentService(Payment.Type.PAYMENT)
                .calculateAmount(car.getDailyFee(), days);
        Session session = stripeUtil.createSession(
                price.longValue(),
                car.getBrand() + " " + car.getModel());
        Payment payment = createPayment(price
                .multiply(BigDecimal.valueOf(0.01))
                .setScale(2,
                        RoundingMode.HALF_UP),
                rental, session, user);
        paymentRepository.save(payment);
        return paymentMapper.toResponseDto(payment);
    }

    @Override
    public PaymentResponseDto success(User user) {
        Payment payment = updateStatus(Payment.Status.PAID, user.getId());
        if (rentalRepository.findRentalByStatusAndUserId(
                Rental.Status.PENDING,
                user.getId()).isPresent()) {
            Rental rental =
                    updateRentalStatus(payment, Rental.Status.LASTING);
            rentalNotificationStrategy.getNotificationService(
                            TELEGRAM, RENTAL_CREATION
                    )
                    .sendMessage(rental, null);
        }
        paymentRepository.save(payment);
        paymentNotificationStrategy.getNotificationService(
                TELEGRAM, SUCCESSFUL_PAYMENT
                )
                .sendMessage(payment, null);

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    public void cancel(User user) {
        Payment payment = updateStatus(Payment.Status.CANCELED, user.getId());
        paymentRepository.save(payment);
        Rental rental = updateRentalStatus(payment, Rental.Status.CANCELED);
        rentalRepository.save(rental);
        updateInventory(rental);
    }

    @Override
    public PaymentResponseDto getMyPayment(User user) {
        Payment payment = paymentRepository.findByStatusAndUserId(
                Payment.Status.PENDING,
                        user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "You don't have an active payment"));
        return paymentMapper.toResponseDto(payment);
    }

    private Payment createPayment(
            BigDecimal price,
            Rental rental,
            Session session,
            User user)
            throws MalformedURLException {
        return new Payment.Builder()
                .setType(Payment.Type.PAYMENT)
                .setAmountToPay(price)
                .setRentalId(rental.getId())
                .setSessionId(session.getId())
                .setSessionUrl(new URL(session.getUrl()))
                .setStatus(Payment.Status.PENDING)
                .setUserId(user.getId())
                .build();
    }

    private void updateInventory(Rental rental) {
        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find a car by id " + rental.getCarId()));
        car.setInventory(car.getInventory() + ONE);
        carRepository.save(car);
    }

    private Rental updateRentalStatus(Payment payment, Rental.Status status) {
        Rental rental = rentalRepository.findById(payment.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find a rental by id " + payment.getRentalId()));
        rental.setStatus(status);
        rentalRepository.save(rental);
        return rental;
    }

    private Payment updateStatus(Payment.Status status, Long userId) {
        Payment payment = paymentRepository
                .findByStatusAndUserId(Payment.Status.PENDING, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user's payment"));
        payment.setStatus(status);
        return payment;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    private void markAllExpiredRentalsAndPaymentsAsCanceled() {
        rentalRepository.findAllByStatusAndRentalDate(
                        Rental.Status.PENDING,
                        LocalDate.now().minusDays(ONE)
                )
                .forEach(rental -> {
                    rental.setStatus(Rental.Status.CANCELED);
                    rental.setDeleted(true);
                    rentalRepository.save(rental);
                    Car car = carRepository.findById(rental.getCarId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Can't find a car by id " + rental.getCarId()));
                    car.setInventory(car.getInventory() + ONE);
                    carRepository.save(car);
                    paymentRepository.findByRentalId(rental.getId())
                            .ifPresent((payment) -> {
                                if (!payment.getType().equals(Payment.Type.FINE)) {
                                    payment.setStatus(Payment.Status.EXPIRED);
                                    payment.setDeleted(true);
                                    paymentRepository.save(payment);
                                }
                            });
                });
    }
}
