package car.sharing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@Data
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private RoleName name = RoleName.ROLE_CUSTOMER;

    public Role(Long id) {
        this.id = id;
    }

    @Override
    public String getAuthority() {
        return name.name();
    }

    public enum RoleName {
        ROLE_CUSTOMER,
        ROLE_MANAGER;

        public static RoleName fromString(String value) {
            for (RoleName role : RoleName.values()) {
                if (role.name().equalsIgnoreCase(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }

        @Override
        public String toString() {
            return name();
        }
    }
}

