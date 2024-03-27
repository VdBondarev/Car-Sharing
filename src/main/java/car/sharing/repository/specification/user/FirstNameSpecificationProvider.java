package car.sharing.repository.specification.user;

import car.sharing.model.User;
import car.sharing.repository.specification.LikeSpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class FirstNameSpecificationProvider implements LikeSpecificationProvider<User> {
    private static final String FIRST_NAME_COLUMN = "firstName";
    private static final String PERCENT = "%";

    @Override
    public Specification<User> getSpecification(String param) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(
                        root.get(FIRST_NAME_COLUMN), PERCENT + param + PERCENT);
    }

    @Override
    public String getKey() {
        return FIRST_NAME_COLUMN;
    }
}
