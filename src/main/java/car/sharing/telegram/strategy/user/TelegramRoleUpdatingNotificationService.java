package car.sharing.telegram.strategy.user;

import car.sharing.model.Role;
import car.sharing.model.User;
import car.sharing.telegram.notification.AbstractNotificationSender;
import car.sharing.telegram.strategy.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class TelegramRoleUpdatingNotificationService
        extends AbstractNotificationSender
        implements NotificationService<User> {
    private static final String TELEGRAM = "telegram";
    private static final String ROLE_UPDATING = "Role updating";

    public TelegramRoleUpdatingNotificationService() {
        super();
    }

    @Override
    public void sendMessage(User user, Long chatId) {
        String message = "User roles were updated to "
                + user.getRoles()
                .stream()
                .map(Role::getName)
                .map(Role.RoleName::toString)
                .toList()
                + " , user id is "
                + user.getId();
        sendMessage(TELEGRAM, chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService, String messageType) {
        return notificationService.equalsIgnoreCase(TELEGRAM)
                && messageType.equalsIgnoreCase(ROLE_UPDATING);
    }
}
