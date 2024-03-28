package car.sharing.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import car.sharing.holder.LinksHolder;
import car.sharing.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentalRepositoryTest extends LinksHolder {
    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @Sql(scripts = {
            ADD_RENTALS_FILE_NAME,
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            REMOVE_RENTALS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Verify that findAllWhereActualReturnDateIsNotNull() method works
            as expected with valid params
            """)
    void findAllWhereActualReturnDateIsNotNull_ValidParams_ReturnsValidResponse() {
        Rental firstRental = createRental(
                "2024-04-01",
                "2024-04-10",
                "2024-04-10",
                1L,
                2L,
                Rental.Status.RETURNED);
        firstRental.setId(2L);

        Rental secondRental = createRental(
                "2024-04-10",
                "2024-04-15",
                "2024-04-15",
                1L,
                2L,
                Rental.Status.RETURNED);
        secondRental.setId(3L);

        List<Rental> expected = List.of(firstRental, secondRental);

        List<Rental> actual = rentalRepository.findAllWhereActualReturnDateIsNotNull(
                2L,
                PageRequest.of(0, 5));

        assertEquals(expected, actual);
    }

    @Test
    @Sql(scripts = {
            ADD_RENTALS_FILE_NAME,
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            REMOVE_RENTALS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Verify that findAllByUserId() method works as expected with valid params
            """)
    void findAllByUserId_ValidParam_ReturnsValidResponse() {
        Rental firstRental = createRental(
                "2024-04-01",
                "2024-04-10",
                "2024-04-10",
                1L,
                2L,
                Rental.Status.RETURNED);
        firstRental.setId(2L);

        Rental secondRental = createRental(
                "2024-04-10",
                "2024-04-15",
                "2024-04-15",
                1L,
                2L,
                Rental.Status.RETURNED);
        secondRental.setId(3L);

        List<Rental> expected = List.of(firstRental, secondRental);

        List<Rental> actual = rentalRepository.findAllByUserId(
                2L,
                PageRequest.of(0, 5));

        assertEquals(expected, actual);
    }

    @Test
    @Sql(scripts = {
            ADD_RENTALS_FILE_NAME,
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            REMOVE_RENTALS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Verify that findRentalByStatusAndUserId() method works as expected with valid params
            """)
    void findRentalByStatusAndUserId() {
        Rental expected = createRental(
                "2024-04-01",
                "2024-04-10",
                null,
                1L,
                1L,
                Rental.Status.LASTING);
        expected.setId(1L);

        Optional<Rental> actual =
                rentalRepository.findRentalByStatusAndUserId(
                        Rental.Status.LASTING,
                        expected.getUserId());

        if (actual.isEmpty()) {
            fail();
        }

        assertEquals(expected, actual.get());
    }

    @Test
    @Sql(scripts = {
            ADD_RENTALS_FILE_NAME,
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            REMOVE_RENTALS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Verify that findRentalByStatusAndUserId() method works as expected with valid params
            """)
    void findAllOverdueRentals_ValidParam_ReturnsValidResponse() {
        Rental firstRental = createRental(
                "2024-04-01",
                "2024-04-10",
                null,
                1L,
                1L,
                Rental.Status.LASTING);
        firstRental.setId(1L);

        Rental secondRental = createRental(
                "2024-04-01",
                "2024-04-10",
                null,
                1L,
                3L,
                Rental.Status.LASTING);
        secondRental.setId(4L);

        List<Rental> expected = List.of(firstRental, secondRental);

        List<Rental> actual =
                rentalRepository.findAllOverdueRentals(LocalDate.now().plusYears(5));

        assertEquals(expected, actual);
    }

    @Test
    @Sql(scripts = {
            ADD_RENTALS_FILE_NAME,
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            REMOVE_RENTALS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Verify that findAllByStatus() method works as expected with valid params
            """)
    void findAllByStatus_ValidParams_ReturnsValidResponse() {
        Rental firstRental = createRental(
                "2024-04-01",
                "2024-04-10",
                null,
                1L,
                1L,
                Rental.Status.LASTING);
        firstRental.setId(1L);

        Rental secondRental = createRental(
                "2024-04-01",
                "2024-04-10",
                null,
                1L,
                3L,
                Rental.Status.LASTING);
        secondRental.setId(4L);

        List<Rental> expected = List.of(firstRental, secondRental);

        List<Rental> actual =
                rentalRepository.findAllByStatus(
                        Rental.Status.LASTING,
                        PageRequest.of(0, 5));

        assertEquals(expected, actual);
    }

    @Test
    @Sql(scripts = {
            ADD_RENTALS_FILE_NAME,
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            REMOVE_RENTALS_FILE_NAME
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Verify that findActiveByUserId() method works as expected with valid params
            """)
    void findActiveByUserId_ValidParams_ReturnsValidResponse() {
        Rental expected = createRental(
                "2024-04-01",
                "2024-04-10",
                null,
                1L,
                1L,
                Rental.Status.LASTING);
        expected.setId(1L);

        Optional<Rental> actual = rentalRepository.findActiveByUserId(1L);

        assertEquals(expected, actual.get());
    }

    private Rental createRental(
            String rentalDate,
            String requiredReturnDate,
            String actualReturnDate,
            Long carId,
            Long userId,
            Rental.Status status) {
        return new Rental.Builder()
                .setRentalDate(LocalDate.parse(rentalDate))
                .setUserId(userId)
                .setStatus(status)
                .setCarId(carId)
                .setRequiredReturnDate(LocalDate.parse(requiredReturnDate))
                .setActualReturnDate(
                        actualReturnDate != null ? LocalDate.parse(actualReturnDate) : null)
                .build();
    }
}
