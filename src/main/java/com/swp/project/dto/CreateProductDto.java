package com.swp.project.dto;

import java.util.List;
import org.hibernate.validator.constraints.Length;
import com.swp.project.entity.product.ProductUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductDto {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Length(max = 50, message = "Tên sản phẩm không được vượt quá 50 ký tự")
    private String name;

    @NotBlank(message = "Tên chú thích không được để trống")
    @Length(max = 250, message = "Không được vượt quá 250 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá sản phẩm phải là số nguyên dương hoặc bằng 0")
    private Integer price;

    @NotNull(message = "Đơn vị sản phẩm không được để trống")
    private ProductUnit unit;
    
    private String main_image_url;

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private boolean enabled = true;

    private List<String> sub_images;

    private List<Long> categories;
}
