package car.sharing.repository.specification;

import org.springframework.data.jpa.domain.Specification;

public interface LikeSpecificationProvider<T> {
    Specification<T> getSpecification(String param);

    String getKey();
}
