package car.sharing.telegram.strategy.response.impl;

import car.sharing.model.Rental;
import car.sharing.repository.RentalRepository;
import car.sharing.telegram.strategy.response.AdminResponseService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminGetRentalResponseService implements AdminResponseService {
    private static final String RENTAL_REGEX =
            "^(?i)Get info about a rental with id:\\s*\\d+$";
    private final RentalRepository rentalRepository;

    @Override
    public String getMessage(String text) {
        Long rentalId = getId(text);
        Optional<Rental> rental = rentalRepository.findById(rentalId);
        if (rental.isEmpty()) {
            return String.format("There is no rental by id %s.", rentalId);
        }
        String message = """
                    ***
                    Found this rental.
                    
                    Car id: %s,
                    User id: %s,
                    Status: %s
                    Rental date: %s,
                    Required return date: %s,
                    Actual return date: %s.
                    """;
        return String.format(
                message,
                rental.get().getCarId(),
                rental.get().getUserId(),
                rental.get().getStatus(),
                rental.get().getRentalDate(),
                rental.get().getRequiredReturnDate(),
                rental.get().getActualReturnDate());
    }

    @Override
    public boolean isApplicable(String text) {
        return text.matches(RENTAL_REGEX);
    }
}
