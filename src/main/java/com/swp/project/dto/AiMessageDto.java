package com.swp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AiMessageDto {
    private String author;
    private String content;
    private String mimeType;
    private String image;

    public AiMessageDto(String author, String content){
        this.author = author;
        this.content = content;
    }
}