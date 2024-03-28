package car.sharing.dto.rental;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalResponseDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate requiredReturnDate;
    private String actualReturnDate;
    private Long carId;
    private Long userId;
}
