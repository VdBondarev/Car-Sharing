package car.sharing.telegram.strategy.response;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminResponseStrategy {
    private final List<AdminResponseService> adminResponseServices;

    public AdminResponseService getResponseService(String text) {
        return adminResponseServices
                .stream()
                .filter(service -> service.isApplicable(text))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find a response service for text " + text));
    }
}
