package com.swp.project.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RevenueDto {
    private String date;
    private Long revenue;
    private Double growthPercent;

}
