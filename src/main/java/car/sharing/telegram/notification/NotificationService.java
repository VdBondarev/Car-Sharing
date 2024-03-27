package car.sharing.telegram.notification;

public interface NotificationService {
    void sendMessage(Long chatId, String message);

    boolean isApplicable(String notificationService);
}
