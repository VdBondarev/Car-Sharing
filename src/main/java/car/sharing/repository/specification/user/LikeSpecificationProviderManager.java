package car.sharing.repository.specification.user;

public interface LikeSpecificationProviderManager<T> {
    LikeSpecificationProvider<T> getSpecificationProvider(String key);
}
