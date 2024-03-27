package car.sharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CarSharing {

    public static void main(String[] args) {
        SpringApplication.run(CarSharing.class, args);
    }
}
