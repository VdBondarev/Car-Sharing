package car.sharing.telegram.notification;

import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class AbstractNotificationSender {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String MESSAGE_SEPARATOR = "***";
    @Autowired
    private List<NotificationService> notificationServices;

    protected void sendMessage(String notificationService,
                               Long chatId,
                               String message) {
        /**
         * Message is changed for better and easier formatting and looking for messages in bot
         */
        message = new StringBuilder(MESSAGE_SEPARATOR)
                .append(LINE_SEPARATOR)
                .append(message)
                .append(LINE_SEPARATOR)
                .append(MESSAGE_SEPARATOR)
                .toString();
        notificationServices
                .stream()
                .filter(service -> service.isApplicable(notificationService))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException(
                                String.format(
                                        "Can't find %s notification service",
                                        notificationService)))
                .sendMessage(chatId, message);
    }
}
