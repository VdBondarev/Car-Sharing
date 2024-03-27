package car.sharing.dto.car;

import java.math.BigDecimal;

public record CarUpdateDto(
        String model,
        String brand,
        Integer inventory,
        BigDecimal dailyFee,
        String type
) {
}
