package car.sharing.repository.specification.car;

import car.sharing.model.Car;
import car.sharing.repository.specification.InSpecificationProvider;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ModelSpecificationProvider implements InSpecificationProvider<Car> {
    private static final String MODEL_COLUMN = "model";

    @Override
    public Specification<Car> getSpecification(List<String> params) {
        return (root, query, criteriaBuilder) -> root.get(MODEL_COLUMN).in(params);
    }

    @Override
    public String getKey() {
        return MODEL_COLUMN;
    }
}
