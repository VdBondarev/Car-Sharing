package car.sharing.repository.specification.car;

import car.sharing.model.Car;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TypeSpecificationProvider {
    private static final String TYPE_COLUMN = "type";

    public Specification<Car> getSpecification(List<String> params) {
        List<Car.Type> types = params.stream()
                .map(Car.Type::fromString)
                .collect(Collectors.toList());
        return (root, query, criteriaBuilder) -> root.get(TYPE_COLUMN).in(types);
    }
}
