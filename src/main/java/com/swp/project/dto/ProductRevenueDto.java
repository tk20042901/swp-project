package com.swp.project.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRevenueDto {
    private Long productId;
    private String productName;
    private String mainImageUrl;
    private Double totalSold;
    private Long revenue;
}
