package org.egov.im.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AdditionalDetailValidator.class)
@Target( {ElementType.METHOD,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CharacterConstraint {

    String message() default "Invalid Additional Details";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int size();


}