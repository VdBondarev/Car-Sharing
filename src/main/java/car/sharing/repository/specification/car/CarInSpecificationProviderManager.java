package car.sharing.repository.specification.car;

import car.sharing.model.Car;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarInSpecificationProviderManager
        implements InSpecificationProviderManager<Car> {
    private final List<InSpecificationProvider<Car>> specificationProviders;

    @Override
    public InSpecificationProvider<Car> getSpecificationProvider(String key) {
        return specificationProviders
                .stream()
                .filter(provider -> provider.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Can't find correct specification for key " + key));
    }
}
