package com.swp.project.dto;

import com.swp.project.entity.user.Customer;
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
    @Pattern(regexp = "^([\\p{L}\\p{N}.\\- ])+$", message = "Tên không hợp lệ")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Bạn chưa chọn tỉnh/thành phố")
    private String provinceCityCode;

    @NotBlank(message = "Bạn chưa chọn xã/phường")
    private String communeWardCode;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 100, message = "Địa chỉ chi tiết không được vượt quá 100 ký tự")
    @Pattern(regexp = "^([\\p{L}\\p{N}.\\- ])+$", message = "Địa chỉ chi tiết không hợp lệ")
    private String specificAddress;

    public boolean setFromExistedInfo(Customer customer) {
        if(customer.getFullName() == null) return false;
        this.setFullName(customer.getFullName());
        this.setPhone(customer.getPhoneNumber());
        this.setProvinceCityCode(customer.getCommuneWard().getProvinceCity().getCode());
        this.setCommuneWardCode(customer.getCommuneWard().getCode());
        this.setSpecificAddress(customer.getSpecificAddress());
        return true;
    }
}
