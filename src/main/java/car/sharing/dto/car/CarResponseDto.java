package car.sharing.dto.car;

import car.sharing.model.Car;
import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        Car.Type type,
        Integer inventory,
        BigDecimal dailyFee
) {

}
