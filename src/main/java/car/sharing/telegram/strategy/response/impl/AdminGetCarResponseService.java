package car.sharing.telegram.strategy.response.impl;

import car.sharing.model.Car;
import car.sharing.repository.CarRepository;
import car.sharing.telegram.strategy.response.AdminResponseService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminGetCarResponseService implements AdminResponseService {
    private static final String CAR_REGEX =
            "^(?i)Get info about a car with id:\\s*\\d+$";
    private final CarRepository carRepository;

    @Override
    public String getMessage(String text) {
        Long carId = getId(text);
        Optional<Car> car = carRepository.findById(carId);
        if (car.isEmpty()) {
            return String.format("There is no car by id %s.", carId);
        }
        String message = """
                    ***
                    Found this car.
                    
                    Brand: %s,
                    Model: %s.
                    ***
                    """;
        return String.format(
                message,
                car.get().getBrand(),
                car.get().getModel());
    }

    @Override
    public boolean isApplicable(String text) {
        return text.matches(CAR_REGEX);
    }
}
