package car.sharing.repository.specification.car;

public interface InSpecificationProviderManager<T> {
    InSpecificationProvider<T> getSpecificationProvider(String key);
}
