package com.swp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class AiMessageDto implements Serializable {
    private String author;
    private String content;
    private String mimeType;
    private String image;

    public AiMessageDto(String author, String content){
        this.author = author;
        this.content = content;
    }
}