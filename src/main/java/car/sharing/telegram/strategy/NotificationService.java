package car.sharing.telegram.strategy;

public interface NotificationService<T> {
    void sendMessage(T type, Long chatId);

    boolean isApplicable(String notificationService, String messageType);
}
