package car.sharing.repository.specification;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public interface InSpecificationProvider<T> {
    Specification<T> getSpecification(List<String> params);

    String getKey();
}
