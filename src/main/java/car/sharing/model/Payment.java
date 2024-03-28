package car.sharing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URL;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Data
@Table(name = "payments")
@SQLDelete(sql = "UPDATE payments SET is_deleted = TRUE WHERE id = ?")
@Where(clause = "is_deleted = FALSE")
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    @Column(name = "rental_id")
    private Long rentalId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Type type;

    @NotNull
    @Column(name = "session_url")
    @EqualsAndHashCode.Exclude
    private URL sessionUrl;

    @NotBlank
    @Column(name = "session_id")
    @EqualsAndHashCode.Exclude
    private String sessionId;

    @NotNull
    @Column(name = "amount_to_pay")
    private BigDecimal amountToPay;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    private Payment(Builder builder) {
        this.amountToPay = builder.amountToPay;
        this.rentalId = builder.rentalId;
        this.userId = builder.userId;
        this.sessionId = builder.sessionId;
        this.sessionUrl = builder.sessionUrl;
        this.type = builder.type;
        this.status = builder.status;
    }

    public enum Type {
        PAYMENT,
        FINE;
        public static Car.Type fromString(String value) {
            for (Car.Type type : Car.Type.values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }
    }

    public enum Status {
        PENDING,
        CANCELED,
        EXPIRED,
        PAID;

        public static Status fromString(String value) {
            for (Status status: Status.values()) {
                if (status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }
    }

    public static class Builder {
        private Long userId;
        private Long rentalId;
        private Status status;
        private Type type;
        private URL sessionUrl;
        private String sessionId;
        private BigDecimal amountToPay;

        public Builder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setRentalId(Long rentalId) {
            this.rentalId = rentalId;
            return this;
        }

        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setSessionUrl(URL sessionUrl) {
            this.sessionUrl = sessionUrl;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder setAmountToPay(BigDecimal amountToPay) {
            this.amountToPay = amountToPay;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
