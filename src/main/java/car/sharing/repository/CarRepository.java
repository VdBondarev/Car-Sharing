package car.sharing.repository;

import car.sharing.model.Car;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>,
        JpaSpecificationExecutor<Car> {
    @Query("FROM Car car WHERE car.inventory > 0")
    List<Car> findAllAvailable(Pageable pageable);
}
