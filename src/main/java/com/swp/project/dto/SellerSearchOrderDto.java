package com.swp.project.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Getter
@Setter
public class SellerSearchOrderDto {
    private Long statusId;

    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String customerEmail;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @Pattern(regexp = "^[1-9]\\d*$", message = "Trang phải là số nguyên dương")
    private String goToPage = "1";

    public boolean isEmpty() {
        return statusId == null
                && (customerEmail == null || customerEmail.isBlank())
                && fromDate == null
                && toDate == null
                && goToPage.equals("1");
    }
}
