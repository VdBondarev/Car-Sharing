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

    private Rental(Builder builder) {
        this.actualReturnDate = builder.actualReturnDate;
        this.carId = builder.carId;
        this.rentalDate = builder.rentalDate;
        this.requiredReturnDate = builder.requiredReturnDate;
        this.userId = builder.userId;
        this.status = builder.status;
    }

    public static class Builder {
        private LocalDate rentalDate;
        private LocalDate requiredReturnDate;
        private LocalDate actualReturnDate;
        private Long carId;
        private Long userId;
        private Status status;

        public Builder setRentalDate(LocalDate rentalRate) {
            this.rentalDate = rentalRate;
            return this;
        }

        public Builder setRequiredReturnDate(LocalDate requiredReturnDate) {
            this.requiredReturnDate = requiredReturnDate;
            return this;
        }

        public Builder setActualReturnDate(LocalDate actualReturnDate) {
            this.actualReturnDate = actualReturnDate;
            return this;
        }

        public Builder setCarId(Long carId) {
            this.carId = carId;
            return this;
        }

        public Builder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Rental build() {
            return new Rental(this);
        }

        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }
    }
}
