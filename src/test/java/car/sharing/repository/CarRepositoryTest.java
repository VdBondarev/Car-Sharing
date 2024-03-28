package car.sharing.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import car.sharing.holder.LinksHolder;
import car.sharing.model.Car;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarRepositoryTest extends LinksHolder {
    @Autowired
    private CarRepository carRepository;

    @Test
    @Sql(scripts = {
            ADD_CARS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            REMOVE_CARS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Verify that findAllAvailable works as expected with valid params")
    void findAllAvailable_ValidPageable_ReturnsValidResponse() {
        Car toyotaCamry = createCar(
                1L, "Toyota",
                "Camry",
                Car.Type.SEDAN,
                BigDecimal.valueOf(9.00));

        Car teslaModelS = createCar(
                2L, "Tesla",
                "Model S",
                Car.Type.UNIVERSAL,
                BigDecimal.valueOf(15.99)
        );
        List<Car> expectedList = List.of(toyotaCamry, teslaModelS);

        List<Car> actualList =
                carRepository.findAllAvailable(PageRequest.of(0, 5));

        assertEquals(expectedList, actualList);
        assertEquals(expectedList.get(0), actualList.get(0));
        assertEquals(expectedList.get(1), actualList.get(1));

        expectedList = List.of();
        actualList =
                carRepository.findAllAvailable(PageRequest.of(1, 5));

        assertEquals(expectedList, actualList);
        assertTrue(actualList.isEmpty());
    }

    private Car createCar(Long id, String brand, String model, Car.Type type, BigDecimal dailyFee) {
        return Car.builder()
                .model(model)
                .type(type)
                .inventory(10)
                .dailyFee(dailyFee)
                .brand(brand)
                .id(id)
                .build();
    }
}
