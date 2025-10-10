package com.swp.project.dto;

import java.util.List;

import com.swp.project.entity.product.ProductUnit;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateProductDto{
    private Long id;

    @Size(max = 50, message = "Tên sản phẩm không được vượt quá 50 ký tự")
    private String name;
    @Size(max = 255, message = "Mô tả sản phẩm không được vượt quá 255 ký tự")
    private String description;

    private Integer price;

    private ProductUnit unit;

    private Boolean enabled;

    private List<Long> categories;

    private String mainImage; // URL của ảnh chính
}
