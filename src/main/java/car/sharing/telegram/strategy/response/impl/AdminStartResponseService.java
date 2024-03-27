package car.sharing.telegram.strategy.response.impl;

import car.sharing.telegram.strategy.response.AdminResponseService;
import org.springframework.stereotype.Service;

@Service
public class AdminStartResponseService implements AdminResponseService {
    @Override
    public String getMessage(String text) {
        return """
                ***
                Hello, this is a car sharing bot.
                
                It is created for admins only.
                
                To find out more, click this button:
                /help
                ***
                """;
    }

    @Override
    public boolean isApplicable(String text) {
        return text.equalsIgnoreCase("/start");
    }
}
