package car.sharing.repository.specification.car;

import car.sharing.model.Car;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class BrandSpecificationProvider implements InSpecificationProvider<Car> {
    private static final String BRAND_COLUMN = "brand";

    @Override
    public Specification<Car> getSpecification(List<String> params) {
        return (root, query, criteriaBuilder) -> root.get(BRAND_COLUMN).in(params);
    }

    @Override
    public String getKey() {
        return BRAND_COLUMN;
    }
}
