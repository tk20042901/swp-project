package com.swp.project.dto;

import org.hibernate.validator.constraints.Length;
import com.swp.project.entity.product.ProductUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductUnitDto {

    @NotNull(message = "ID đơn vị không được để trống")
    private Long id;

    @NotBlank(message = "Tên đơn vị không được để trống")
    @Length(max = 20, message = "Tên đơn vị không được vượt quá 20 ký tự")
    private String name;

    @NotNull(message = "Trường cho phép thập phân không được để trống")
    private Boolean isAllowDecimal;

    @NotNull(message = "Trường trạng thái không được để trống")
    private Boolean isActive;

    public UpdateProductUnitDto(ProductUnit productUnit) {
        this.id = productUnit.getId();
        this.name = productUnit.getName();
        this.isAllowDecimal = productUnit.isAllowDecimal();
        this.isActive = productUnit.isActive();
    }
}
