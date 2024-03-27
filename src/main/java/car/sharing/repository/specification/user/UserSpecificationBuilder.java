package car.sharing.repository.specification.user;

import car.sharing.dto.user.UserSearchParametersDto;
import car.sharing.model.User;
import car.sharing.repository.specification.SpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserSpecificationBuilder
        implements SpecificationBuilder<User, UserSearchParametersDto> {
    private static final String EMAIL_COLUMN = "email";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private final UserLikeSpecificationProviderManager likeSpecificationProviderManager;

    @Override
    public Specification<User> build(UserSearchParametersDto parametersDto) {
        Specification<User> specification = Specification.where(null);
        if (notEmpty(parametersDto.email())) {
            specification = getLikeSpecification(
                    EMAIL_COLUMN, specification, parametersDto.email());
        }
        if (notEmpty(parametersDto.firstName())) {
            specification = getLikeSpecification(
                    FIRST_NAME, specification, parametersDto.firstName());
        }
        if (notEmpty(parametersDto.lastName())) {
            specification = getLikeSpecification(
                    LAST_NAME, specification, parametersDto.lastName());
        }
        return specification;
    }

    private Specification<User> getLikeSpecification(
            String column,
            Specification<User> specification,
            String param) {
        return specification.and(likeSpecificationProviderManager
                .getSpecificationProvider(column)
                .getSpecification(param));
    }

    private boolean notEmpty(String param) {
        return param != null && !param.isEmpty();
    }
}
