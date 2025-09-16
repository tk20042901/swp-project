package com.swp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryInfoDto {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    private String phone;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 100, message = "Địa chỉ chi tiết không được vượt quá 100 ký tự")
    private String address;

    @NotBlank(message = "Bạn chưa chọn xã/phường")
    private String ward;

}
