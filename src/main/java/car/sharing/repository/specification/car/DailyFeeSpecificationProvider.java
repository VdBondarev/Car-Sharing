package car.sharing.repository.specification.car;

import car.sharing.model.Car;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class DailyFeeSpecificationProvider {
    private static final String PRICE_COLUMN = "dailyFee";

    public Specification<Car> getSpecification(List<BigDecimal> params) {
        BigDecimal priceFrom;
        BigDecimal priceTo;
        if (params.size() == 1) {
            priceFrom = BigDecimal.ZERO;
            priceTo = params.get(0);
        } else {
            priceFrom = params.get(0);
            priceTo = params.get(1);
        }
        if (priceFrom.compareTo(priceTo) >= 0) {
            throw new IllegalArgumentException(
                    "Price to should be greater than price price from, but was " + params);
        }
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.between(
                        root.get(PRICE_COLUMN),
                priceFrom,
                priceTo);
    }
}
