package com.swp.project.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateShoppingCartDto {
    private Long productId;
    @NotNull(message = "Số lượng không được để trống")
    @Min(value=1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    @Pattern(regexp = "^[0-9]+$", message = "Chỉ được nhập số nguyên")
    private String quantity;

}
