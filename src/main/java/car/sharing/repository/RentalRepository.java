package car.sharing.repository;

import car.sharing.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query("FROM Rental rental "
            + "WHERE rental.userId = :userId AND rental.actualReturnDate IS NOT NULL")
    List<Rental> findAllWhereActualReturnDateIsNotNull(Long userId, Pageable pageable);

    List<Rental> findAllByUserId(Long userId, Pageable pageable);

    Optional<Rental> findRentalByStatusAndUserId(Rental.Status status, Long userId);

    @Query("FROM Rental rental "
            + "WHERE rental.actualReturnDate IS NULL "
            + "AND rental.requiredReturnDate < :now "
            + "AND rental.status = 'LASTING'")
    List<Rental> findAllOverdueRentals(LocalDate now);

    List<Rental> findAllByStatus(Rental.Status status, Pageable pageable);

    @Query("FROM Rental rental "
            + "WHERE (rental.status = 'PENDING' OR rental.status = 'LASTING') "
            + "AND rental.userId = :userId")
    Optional<Rental> findActiveByUserId(Long userId);

    @Query("FROM Rental rental WHERE rental.status = :status AND rental.rentalDate <= :localDate")
    List<Rental> findAllByStatusAndRentalDate(Rental.Status status, LocalDate localDate);
}
