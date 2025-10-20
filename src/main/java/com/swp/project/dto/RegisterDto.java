package com.swp.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @Size(min = 6, max = 255, message = "Mật khẩu phải có độ dài từ 6 đến 255 ký tự")
    private String password;

    @Size(min = 6, max = 255, message = "Xác nhận mật khẩu phải có độ dài từ 6 đến 255 ký tự")
    private String confirmPassword;
    
}