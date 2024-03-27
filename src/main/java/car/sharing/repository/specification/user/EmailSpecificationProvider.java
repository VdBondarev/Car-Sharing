package car.sharing.repository.specification.user;

import car.sharing.model.User;
import car.sharing.repository.specification.LikeSpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class EmailSpecificationProvider implements LikeSpecificationProvider<User> {
    private static final String EMAIL_COLUMN = "email";
    private static final String PERCENT = "%";

    @Override
    public Specification<User> getSpecification(String param) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(
                        root.get(EMAIL_COLUMN), PERCENT + param + PERCENT);
    }

    @Override
    public String getKey() {
        return EMAIL_COLUMN;
    }
}
