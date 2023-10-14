package com.frankisko.clipcat.validations;

import java.io.File;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PathExistsValidator implements ConstraintValidator<PathExists, String> {
    @Override
    public void initialize(PathExists constraintAnnotation) {
    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (path == null) {
            return false;
        }

        File folder = new File(path);
        return folder.exists() && folder.isDirectory();
    }
}
