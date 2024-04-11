package car.sharing.repository.specification.user;

import car.sharing.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class LastNameSpecificationProvider implements LikeSpecificationProvider<User> {
    private static final String LAST_NAME_COLUMN = "lastName";
    private static final String PERCENT = "%";

    @Override
    public Specification<User> getSpecification(String param) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(
                        root.get(LAST_NAME_COLUMN), PERCENT + param + PERCENT);
    }

    @Override
    public String getKey() {
        return LAST_NAME_COLUMN;
    }
}
