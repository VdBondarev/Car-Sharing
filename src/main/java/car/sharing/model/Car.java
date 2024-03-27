package car.sharing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Data
@Table(name = "cars")
@SQLDelete(sql = "UPDATE cars SET is_deleted = TRUE WHERE id = ?")
@Where(clause = "is_deleted = FALSE")
@NoArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String model;

    @NotBlank
    private String brand;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Type type;

    @Min(0)
    private Integer inventory;

    // currency - USD
    @Column(name = "daily_fee")
    @Min(0)
    @EqualsAndHashCode.Exclude
    private BigDecimal dailyFee;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    private Car(Builder builder) {
        this.inventory = builder.inventory;
        this.brand = builder.brand;
        this.model = builder.model;
        this.dailyFee = builder.dailyFee;
        this.type = builder.type;
    }

    public enum Type {
        SEDAN,
        SUV,
        HATCHBACK,
        UNIVERSAL;

        public static Type fromString(String value) {
            for (Type type : Type.values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }
    }

    public static class Builder {
        private String model;
        private String brand;
        @Enumerated(EnumType.STRING)
        private Type type;
        @Min(0)
        private Integer inventory;
        @Min(0)
        private BigDecimal dailyFee;

        public Builder setModel(String model) {
            this.model = model;
            return this;
        }

        public Builder setBrand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setInventory(Integer inventory) {
            this.inventory = inventory;
            return this;
        }

        public Builder setDailyFee(BigDecimal dailyFee) {
            this.dailyFee = dailyFee;
            return this;
        }

        public Car build() {
            return new Car(this);
        }
    }
}
