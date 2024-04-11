package car.sharing.repository.specification.user;

import org.springframework.data.jpa.domain.Specification;

public interface LikeSpecificationProvider<T> {
    Specification<T> getSpecification(String param);

    String getKey();
}
