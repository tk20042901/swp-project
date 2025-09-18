package com.swp.project.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ViewManagerDto {

    private Long id;
    private String email;
    private boolean enabled;
}