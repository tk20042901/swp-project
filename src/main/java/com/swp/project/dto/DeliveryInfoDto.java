package com.swp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DeliveryInfoDto implements Serializable {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{6,12}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Bạn chưa chọn tỉnh/thành phố")
    private String provinceCityCode;

    @NotBlank(message = "Bạn chưa chọn xã/phường")
    private String communeWardCode;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 100, message = "Địa chỉ chi tiết không được vượt quá 100 ký tự")
    private String specificAddress;

}
