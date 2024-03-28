package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.model.Rental;
import java.time.LocalDate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(target = "actualReturnDate", ignore = true)
    RentalResponseDto toResponseDto(Rental rental);

    @AfterMapping
    default void setActualReturnDate(
            @MappingTarget RentalResponseDto responseDto,
            Rental rental) {
        String actualReturnDate;
        if (rental.getActualReturnDate() == null
                && rental.getStatus().equals(Rental.Status.LASTING)) {
            actualReturnDate = "The car is not returned yet."
                    + " Return it in time or you will pay 3x for each day after required day.";
        } else if (rental.getActualReturnDate() == null) {
            actualReturnDate = "Your rental is not active yet. Pay for that first.";
        } else if (!rental.getRequiredReturnDate().isBefore(LocalDate.now())) {
            actualReturnDate = rental.getActualReturnDate().toString();
        } else {
            actualReturnDate = rental.getActualReturnDate().toString()
                    + ". Car is returned not in required time. "
                    + "You should pay fine. You can't rent a new car until you pay.";
        }
        responseDto.setActualReturnDate(actualReturnDate);
    }
}
