package car.sharing.service.rental;

import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto addRental(User user, Long carId, int daysToRent);

    List<RentalResponseDto> getUserRentals(Long userId, boolean isActive, Pageable pageable);

    RentalResponseDto getSpecificRental(Long id);

    RentalResponseDto setReturnDate(User rentalId);

    List<RentalResponseDto> getAllRentals(User user, Pageable pageable);

    void cancel(User user);

    List<RentalResponseDto> getAllActive(Pageable pageable);
}
