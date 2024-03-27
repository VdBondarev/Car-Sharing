package car.sharing.repository.specification;

public interface LikeSpecificationProviderManager<T> {
    LikeSpecificationProvider<T> getSpecificationProvider(String key);
}
