package com.swp.project.customAnnotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotEqualToValidator implements ConstraintValidator<NotEqualTo, String> {

    private String notEqualToValue;  // Giá trị mà trường không được trùng

    @Override
    public void initialize(NotEqualTo constraintAnnotation) {
        this.notEqualToValue = constraintAnnotation.value();  // Lấy giá trị từ annotation
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // Nếu giá trị là null thì coi là hợp lệ
        }
        return !value.equals(notEqualToValue);  // Kiểm tra xem giá trị có khác với giá trị xác định không
    }
}
