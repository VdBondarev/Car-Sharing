package car.sharing.telegram.strategy.response.impl;

import car.sharing.model.User;
import car.sharing.repository.UserRepository;
import car.sharing.telegram.strategy.response.AdminResponseService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminGetUserResponseService implements AdminResponseService {
    private static final String USER_REGEX =
            "^(?i)Get info about a user with id:\\s*\\d+$";
    private final UserRepository userRepository;

    @Override
    public String getMessage(String text) {
        Long userId = getId(text);
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return String.format("There is no user by id %s.", userId);
        }
        String message = """
                    ***
                    Found this user.
                    
                    First name: %s,
                    Last name: %s,
                    Email: %s.
                    ***
                    """;
        return String.format(
                message,
                user.get().getFirstName(),
                user.get().getLastName(),
                user.get().getEmail());
    }

    @Override
    public boolean isApplicable(String text) {
        return text.matches(USER_REGEX);
    }
}
