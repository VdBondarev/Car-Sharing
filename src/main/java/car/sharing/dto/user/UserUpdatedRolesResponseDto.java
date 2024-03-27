package car.sharing.dto.user;

import java.util.Set;
import lombok.Data;

@Data
public class UserUpdatedRolesResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Long> rolesIds;
}
