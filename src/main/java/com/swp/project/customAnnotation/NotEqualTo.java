package com.swp.project.customAnnotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEqualToValidator.class)  // Định nghĩa validator sẽ sử dụng
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEqualTo {
    String message() default "Giá trị không được trùng với giá trị xác định";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String value();  // Giá trị mà trường không được trùng
}