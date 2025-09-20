package com.swp.project.dto;

import java.io.Serializable;

import com.swp.project.customAnnotation.NotEqualTo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto implements Serializable {
    private static final long serialVersionUID = 1L;

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
    private String Cid;

    private String provinceCity;

    @NotBlank(message = "Phường / xã không để trống")
    private String communeWard;

    @NotBlank(message = "Địa chỉ cụ thể không để trống")
    private String specificAddress;
}
