package car.sharing.telegram.strategy.response.impl;

import car.sharing.telegram.strategy.response.AdminResponseService;
import org.springframework.stereotype.Service;

@Service
public class AdminUnknownResponseService implements AdminResponseService {
    @Override
    public String getMessage(String text) {
        return String.format("Unknown command: '%s'"
                + System.lineSeparator()
                + "Maybe you meant to type /start ?", text);
    }

    @Override
    public boolean isApplicable(String text) {
        return !text.equalsIgnoreCase("/start")
                && !text.equalsIgnoreCase("/help");
    }
}
