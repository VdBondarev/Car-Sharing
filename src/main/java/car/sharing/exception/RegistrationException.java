package car.sharing.exception;

import java.io.IOException;

public class RegistrationException extends IOException {
    public RegistrationException(String message) {
        super(message);
    }
}
