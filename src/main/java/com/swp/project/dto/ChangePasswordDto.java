package com.swp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {

    @NotBlank(message = "Mật khẩu cũ không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải có độ dài từ 6 đến 255 ký tự")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu mới phải có độ dài từ 6 đến 255 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).+$", message = "Mật khẩu phải chứa ít nhất một chữ cái viết hoa, một chữ cái viết thường, một chữ số và một ký tự đặc biệt")
    private String newPassword;

    private String confirmNewPassword;
}
