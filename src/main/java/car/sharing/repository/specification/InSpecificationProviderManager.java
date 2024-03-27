package car.sharing.repository.specification;

public interface InSpecificationProviderManager<T> {
    InSpecificationProvider<T> getSpecificationProvider(String key);
}
