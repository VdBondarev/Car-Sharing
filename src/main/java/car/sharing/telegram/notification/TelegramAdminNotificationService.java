package car.sharing.telegram.notification;

import car.sharing.telegram.CarSharingTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramAdminNotificationService implements NotificationService {
    private final CarSharingTelegramBot telegramBot;
    @Value("${default.telegram.admin.chat.id}")
    private Long chatId;

    @Override
    public void sendMessage(Long chatId, String message) {
        telegramBot.sendMessage(chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService) {
        return notificationService.equalsIgnoreCase("telegram");
    }
}
