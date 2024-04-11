package car.sharing.telegram.strategy.car;

import car.sharing.model.Car;
import car.sharing.telegram.notification.AbstractNotificationSender;
import car.sharing.telegram.strategy.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class TelegramCarDeletionNotificationService
        extends AbstractNotificationSender
        implements NotificationService<Car> {
    private static final String TELEGRAM = "telegram";
    private static final String CAR_DELETION = "Car deletion";

    public TelegramCarDeletionNotificationService() {
        super();
    }

    @Override
    public void sendMessage(Car car, Long chatId) {
        String message = "The car with id " + car.getId() + " is deleted.";
        sendMessage(TELEGRAM, chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService, String messageType) {
        return notificationService.equalsIgnoreCase(TELEGRAM)
                && messageType.equalsIgnoreCase(CAR_DELETION);
    }
}
