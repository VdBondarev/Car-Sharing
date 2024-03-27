package car.sharing.dto.car;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CarSearchParametersDto(
        List<String> models,
        List<String> brands,
        @Size(min = 1, max = 2)
        List<BigDecimal> priceBetween,
        List<String> types
) {
}
