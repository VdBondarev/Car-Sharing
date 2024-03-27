package car.sharing.annotation.validator;

import car.sharing.annotation.FieldMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

public class PasswordMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String passwordFieldName;
    private String repeatPasswordFieldName;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        passwordFieldName = constraintAnnotation.password();
        repeatPasswordFieldName = constraintAnnotation.repeatPassword();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Field passwordField = value.getClass().getDeclaredField(passwordFieldName);
            passwordField.setAccessible(true);
            Object passwordValue = passwordField.get(value);
            Field repeatPasswordField = value.getClass().getDeclaredField(repeatPasswordFieldName);
            repeatPasswordField.setAccessible(true);
            Object repeatPasswordValue = repeatPasswordField.get(value);
            return Objects.equals(passwordValue, repeatPasswordValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error accessing fields", e);
        }
    }
}
