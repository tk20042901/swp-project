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
    private Integer price;
    private String main_image_url;
    // Getters and Setters
}
