package car.sharing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Data
@Table(name = "rentals")
@SQLDelete(sql = "UPDATE rentals SET is_deleted = TRUE WHERE id = ?")
@Where(clause = "is_deleted = FALSE")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "rental_date")
    private LocalDate rentalDate;

    @NotNull
    @Column(name = "required_return_date")
    private LocalDate requiredReturnDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @NotNull
    @Column(name = "car_id")
    private Long carId;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    public enum Status {
        PENDING,
        LASTING,
        RETURNED,
        CANCELED;
        public static Status fromString(String value) {
            for (Status status : Status.values()) {
                if (status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }
    }
}
