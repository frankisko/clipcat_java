package com.frankisko.clipcat.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PathExistsValidator.class)
@Documented
public @interface PathExists {
    String message() default "Path does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
