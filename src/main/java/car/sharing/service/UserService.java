package car.sharing.service;

import car.sharing.dto.UserRegistrationRequestDto;
import car.sharing.dto.UserResponseDto;
import car.sharing.exception.RegistrationException;
import com.stripe.exception.StripeException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException, StripeException;
}
