package car.sharing.telegram.strategy.rental;

import car.sharing.model.Rental;
import car.sharing.telegram.notification.AbstractNotificationSender;
import car.sharing.telegram.strategy.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class TelegramRentalReturningNotificationService
        extends AbstractNotificationSender
        implements NotificationService<Rental> {
    private static final String TELEGRAM = "telegram";
    private static final String RENTAL_RETURNING = "Rental returning";

    public TelegramRentalReturningNotificationService() {
        super();
    }

    @Override
    public void sendMessage(Rental rental, Long chatId) {
        String message = """
                Car is returned.

                Rental id: %s,
                Required return date: %s,
                Actual return date: %s.
                """;
        message = String.format(
                message,
                rental.getId(),
                rental.getRequiredReturnDate(),
                rental.getActualReturnDate());
        sendMessage(TELEGRAM, chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService, String messageType) {
        return notificationService.equalsIgnoreCase(TELEGRAM)
                && messageType.equalsIgnoreCase(RENTAL_RETURNING);
    }
}
