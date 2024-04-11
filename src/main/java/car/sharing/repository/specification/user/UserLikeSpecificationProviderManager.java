package car.sharing.repository.specification.user;

import car.sharing.model.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLikeSpecificationProviderManager
        implements LikeSpecificationProviderManager<User> {
    private final List<LikeSpecificationProvider<User>> specificationProviders;

    @Override
    public LikeSpecificationProvider<User> getSpecificationProvider(String key) {
        return specificationProviders
                .stream()
                .filter(provider -> provider.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Can't find specification for key " + key));
    }
}
