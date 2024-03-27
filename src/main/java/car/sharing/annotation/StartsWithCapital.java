package car.sharing.annotation;

import car.sharing.annotation.validator.StartsWithCapitalValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartsWithCapitalValidator.class)
@Documented
public @interface StartsWithCapital {
    String message() default "should start with a capital letter";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
