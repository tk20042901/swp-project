package com.swp.project.dto;

import java.io.Serializable;

import com.swp.project.customAnnotation.NotEqualTo;

import com.swp.project.entity.user.CustomerSupport;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.service.user.CustomerSupportService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id = 0L;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải có độ dài từ 6 đến 50 ký tự")
    private String password;

    @NotBlank(message = "Tên không được để trống")
    private String fullname;

    @NotBlank(message = "Ngày tháng năm sinh không được để trống")
    private String birthDate;

    @NotBlank(message = "Mã căn cước công dân không được để trống")
    @Pattern(regexp = "\\d{12}", message = "Mã căn cước công dân phải gồm 12 chữ số")
    private String cid;

    private String provinceCity;

    @NotBlank(message = "Phường / xã không để trống")
    private String communeWard;

    @NotBlank(message = "Địa chỉ cụ thể không để trống")
    private String specificAddress;

    private boolean enabled;

    public StaffDto parse(Seller seller) {
        return StaffDto.builder()
                .id(seller.getId())
                .email(seller.getEmail())
                .password(seller.getPassword())
                .fullname(seller.getFullname())
                .birthDate(seller.getBirthDate().toString())
                .cid(seller.getCid())
                .provinceCity(seller.getCommuneWard().getProvinceCity().getCode())
                .communeWard(seller.getCommuneWard().getCode())
                .specificAddress(seller.getSpecificAddress())
                .enabled(seller.isEnabled())
                .build();
    }

    public StaffDto parse(Shipper shipper) {
        return StaffDto.builder()
                .id(shipper.getId())
                .email(shipper.getEmail())
                .password(shipper.getPassword())
                .fullname(shipper.getFullname())
                .birthDate(shipper.getBirthDate().toString())
                .cid(shipper.getCid())
                .provinceCity(shipper.getCommuneWard().getProvinceCity().getCode())
                .communeWard(shipper.getCommuneWard().getCode())
                .specificAddress(shipper.getSpecificAddress())
                .enabled(shipper.isEnabled())
                .build();
    }

    public StaffDto parse(CustomerSupport customerSupport) {
        return StaffDto.builder()
                .id(customerSupport.getId())
                .email(customerSupport.getEmail())
                .password(customerSupport.getPassword())
                .fullname(customerSupport.getFullname())
                .birthDate(customerSupport.getBirthDate().toString())
                .cid(customerSupport.getCid())
                .provinceCity(customerSupport.getCommuneWard().getProvinceCity().getCode())
                .communeWard(customerSupport.getCommuneWard().getCode())
                .specificAddress(customerSupport.getSpecificAddress())
                .enabled(customerSupport.isEnabled())
                .build();
    }
}
