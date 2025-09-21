package com.swp.project.dto;

import lombok.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ViewProductDto {
    
    private Long id;
    private String name;
    private Double price;
    private String mainImageUrl;
    // Getters and Setters
}
