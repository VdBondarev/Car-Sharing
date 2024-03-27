package car.sharing.telegram.strategy.car;

import car.sharing.model.Car;
import car.sharing.telegram.notification.AbstractNotificationSender;
import car.sharing.telegram.strategy.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class TelegramCarCreationNotificationService
        extends AbstractNotificationSender
        implements NotificationService<Car> {
    private static final String TELEGRAM = "telegram";
    private static final String CAR_CREATION = "Car creation";

    @Override
    public void sendMessage(Car car, Long chatId) {
        String message = """
                  A new car is created.
 
                  Id: %s,
                  Brand: %s,
                  Model: %s,
                  Type: %s,
                  Inventory: %s,
                  Daily fee: %s.
                  """;
        message = String.format(
                message,
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getType(),
                car.getInventory(),
                car.getDailyFee());
        sendMessage(TELEGRAM, chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService, String messageType) {
        return notificationService.equalsIgnoreCase(TELEGRAM)
                && messageType.equalsIgnoreCase(CAR_CREATION);
    }
}
