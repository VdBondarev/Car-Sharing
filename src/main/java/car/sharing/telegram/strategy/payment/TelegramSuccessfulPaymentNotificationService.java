package car.sharing.telegram.strategy.payment;

import car.sharing.model.Payment;
import car.sharing.telegram.notification.AbstractNotificationSender;
import car.sharing.telegram.strategy.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class TelegramSuccessfulPaymentNotificationService
        extends AbstractNotificationSender
        implements NotificationService<Payment> {
    private static final String TELEGRAM = "telegram";
    private static final String SUCCESSFUL_PAYMENT = "successful payment";

    @Override
    public void sendMessage(Payment payment, Long chatId) {
        String message = """
                Payment is paid.
                
                Payment id: %s,
                Rental id: %s,
                User id: %s,
                Type: %s
                Paid amount: %s.
                """;
        message = String.format(message,
                payment.getId(),
                payment.getRentalId(),
                payment.getUserId(),
                payment.getType(),
                payment.getAmountToPay());
        sendMessage(TELEGRAM, chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService, String messageType) {
        return notificationService.equalsIgnoreCase(TELEGRAM)
                && messageType.equalsIgnoreCase(SUCCESSFUL_PAYMENT);
    }
}
