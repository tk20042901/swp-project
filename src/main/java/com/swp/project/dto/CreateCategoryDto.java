package com.swp.project.dto;

import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryDto {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Length(max = 50, message = "Tên danh mục không được vượt quá 50 ký tự")
    private String name;

    @NotNull(message = "Trạng thái hoạt động của danh mục không được để trống")
    private Boolean isActive;

}
