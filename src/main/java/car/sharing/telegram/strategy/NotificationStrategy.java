package car.sharing.telegram.strategy;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationStrategy<T> {
    private final List<NotificationService<T>> notificationServices;

    public NotificationService<T> getNotificationService(
            String notificationService,
            String messageType) {
        return notificationServices
                .stream()
                .filter(service -> service.isApplicable(notificationService, messageType))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find service for "
                                + notificationServices
                                + " and "
                                + messageType));
    }
}
