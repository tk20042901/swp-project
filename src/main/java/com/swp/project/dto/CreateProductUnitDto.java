package com.swp.project.dto;

import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductUnitDto {

    @NotBlank(message = "Tên đơn vị không được để trống")
    @Length(max = 20, message = "Tên đơn vị không được vượt quá 20 ký tự")
    private String name;

    @NotNull(message = "Trường cho phép thập phân không được để trống")
    private Boolean isAllowDecimal;

    @NotNull(message = "Trường trạng thái không được để trống")
    private Boolean isActive;
}
