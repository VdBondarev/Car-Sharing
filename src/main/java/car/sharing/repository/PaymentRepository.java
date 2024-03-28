package car.sharing.repository;

import car.sharing.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUserId(Long userId, Pageable pageable);

    Optional<Payment> findByStatusAndUserId(Payment.Status status, Long userId);

    Optional<Payment> findByTypeAndUserIdAndStatus(
            Payment.Type type,
            Long userId,
            Payment.Status status);

    Optional<Payment> findByRentalId(Long id);
}
