package car.sharing.telegram.notification;

import car.sharing.model.Rental;
import car.sharing.repository.RentalRepository;
import car.sharing.telegram.CarSharingTelegramBot;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramAdminNotificationService implements NotificationService {
    private static final String TELEGRAM = "Telegram";
    private final CarSharingTelegramBot telegramBot;
    private final RentalRepository rentalRepository;
    @Value("${default.telegram.admin.chat.id}")
    private Long chatId;

    @Override
    public void sendMessage(Long chatId, String message) {
        telegramBot.sendMessage(chatId, message);
    }

    @Override
    public boolean isApplicable(String notificationService) {
        return notificationService.equalsIgnoreCase(TELEGRAM);
    }

    @Scheduled(cron = "0 0 9 * * *")
    private void remindOfOverdueRentals() {
        List<Rental> overdueRentals =
                rentalRepository.findAllOverdueRentals(LocalDate.now());
        if (!overdueRentals.isEmpty()) {
            overdueRentals
                    .stream()
                    .map(this::createMessageAboutRental)
                    .forEach(message -> sendMessage(chatId, message));
            return;
        }
        sendMessage(chatId, "No rentals overdue today!");
    }

    private String createMessageAboutRental(Rental rental) {
        String message = """
                ***
                The following rental is overdue.
                
                Rental id: %s,
                User id: %s,
                Car id: %s,
                Rental date: %s,
                Required return date: %s.
                ***
                """;
        return String.format(
                message,
                rental.getId(),
                rental.getUserId(),
                rental.getCarId(),
                rental.getRentalDate(),
                rental.getRequiredReturnDate());

    }
}
