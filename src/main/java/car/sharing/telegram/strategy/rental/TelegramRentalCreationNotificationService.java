package car.sharing.telegram.strategy.rental;

import car.sharing.model.Rental;
import car.sharing.telegram.notification.AbstractNotificationSender;
import car.sharing.telegram.strategy.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class TelegramRentalCreationNotificationService
        extends AbstractNotificationSender
        implements NotificationService<Rental> {
    private static final String TELEGRAM = "telegram";
    private static final String RENTAL_CREATION = "Rental creation";

    public TelegramRentalCreationNotificationService() {
        super();
    }

    @Override
    public void sendMessage(Rental rental, Long chatId) {
        String message = """
                A new rent is created.

                Car id: %s,
                User id: %s
                Rental date: %s,
                Expected return date: %s.
                """;
        message = String.format(
                message,
                rental.getCarId(),
                rental.getUserId(),
                rental.getRentalDate(),
                rental.getRequiredReturnDate());
        sendMessage(TELEGRAM, chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService, String messageType) {
        return notificationService.equalsIgnoreCase(TELEGRAM)
                && messageType.equalsIgnoreCase(RENTAL_CREATION);
    }
}
