package com.swp.project.dto;

import java.util.List;

import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.ProductUnit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateProductDto{
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 50, message = "Tên sản phẩm không được vượt quá 50 ký tự")
    private String name;
    @NotBlank(message = "Mô tả sản phẩm không được để trống")
    @Size(max = 255, message = "Mô tả sản phẩm không được vượt quá 255 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @Min(value = 0, message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    private Long price;

    @NotNull(message = "Đơn vị sản phẩm không được để trống")
    private ProductUnit unit;

    @NotNull(message = "Trạng thái sản phẩm không được để trống")
    private Boolean enabled;

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private List<Long> categories;

    @NotNull(message = "Số lượng sản phẩm không được để trống")
    private Double quantity;

    private String subDisplay1;
    private String subDisplay2;
    private String subDisplay3;
    private String mainImage; 
    private List<Category> finalCategories;
}
