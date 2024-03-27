package car.sharing.telegram.strategy.response.impl;

import car.sharing.telegram.strategy.response.AdminResponseService;
import org.springframework.stereotype.Service;

@Service
public class AdminHelpResponseService implements AdminResponseService {
    @Override
    public String getMessage(String text) {
        return """
                ***
                This bot is for admins of car this car sharing application only.
                
                It will send you notifications about main operations happening in the application.
                
                To get info about:
                1). A user
                2). A car
                Send me a message with the following format:
                
                Get info about a user with id: ...
                
                Get info about a car with id: ...
                
                Instead of ... paste id.
                ***
                """;
    }

    @Override
    public boolean isApplicable(String text) {
        return text.equalsIgnoreCase("/help");
    }
}
