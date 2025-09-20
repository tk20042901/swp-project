package com.swp.project.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Getter
@Setter
public class SellerSearchOrderDto {
    private Long statusId;
    private String customerEmail;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    public boolean isEmpty() {
        return statusId == null
                && (customerEmail == null || customerEmail.isBlank())
                && fromDate == null
                && toDate == null;
    }
}
