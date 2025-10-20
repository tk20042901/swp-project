package com.swp.project.service.user;

import com.swp.project.dto.RegisterDto;
import com.swp.project.entity.PendingRegister;
import com.swp.project.repository.PendingRegisterRepository;
import com.swp.project.repository.user.CustomerRepository;
import com.swp.project.repository.user.UserRepository;
import com.swp.project.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PendingRegisterRepository pendingRegisterRepository;

    @SuppressWarnings("unused")
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("Register success when pending register does not exist")
    void register_Success_PendingRegisterNotExists() {

        RegisterDto registerDto = new RegisterDto();

        // password and confirmPassword match
        registerDto.setPassword("password");
        registerDto.setConfirmPassword(registerDto.getPassword());

        // email does not exist in the system
        when(userRepository.existsByEmail(nullable(String.class))).thenReturn(false);

        // pending registration for the email does not exist
        when(pendingRegisterRepository.findByEmail(nullable(String.class))).thenReturn(null);

        customerService.register(registerDto);

        // verify pendingRegisterRepository.delete() is not called
        verify(pendingRegisterRepository,never()).delete(any());

        // verify emailService.sendSimpleEmail() is called
        verify(emailService).sendSimpleEmail(nullable(String.class), anyString(), anyString());

        // verify pendingRegisterRepository.save() is called
        verify(pendingRegisterRepository).save(any());
    }


    @Test
    @DisplayName("Register fails when password and confirmPassword do not match")
    void register_Fail_PasswordAndConfirmPasswordNotMatch() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setPassword("password");
        registerDto.setConfirmPassword(registerDto.getPassword() + "xxx");

        var exception = assertThrows(RuntimeException.class, () -> customerService.register(registerDto));
        assertEquals("Mật khẩu và xác nhận mật khẩu không khớp", exception.getMessage());
    }

    @Test
    @DisplayName("Register fails when email already exists")
    void register_Fail_EmailAlreadyExists() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("test@email.com");

        // password and confirmPassword match
        registerDto.setPassword("password");
        registerDto.setConfirmPassword(registerDto.getPassword());

        // email exists in the system
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        var exception = assertThrows(RuntimeException.class, () -> customerService.register(registerDto));
        assertEquals("Email " + registerDto.getEmail()  + " đã được sử dụng", exception.getMessage());
    }

    @Test
    @DisplayName("Register fails when sending email failed and pending register already exists")
    void register_Fail_SendEmailFailed_PendingRegisterAlreadyExists() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("test@email.com");

        // password and confirmPassword match
        registerDto.setPassword("password");
        registerDto.setConfirmPassword(registerDto.getPassword());

        // email exists in the system
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // pending registration for the email already exists
        when(pendingRegisterRepository.findByEmail(nullable(String.class))).thenReturn(new PendingRegister());

        // mock sending email to throw exception
        doThrow(RuntimeException.class).when(emailService).sendSimpleEmail(nullable(String.class), anyString(), anyString());

        var exception = assertThrows(RuntimeException.class, () -> customerService.register(registerDto));
        assertEquals("Gửi email thất bại. Vui lòng thử lại sau.", exception.getMessage());

        // verify pendingRegisterRepository.delete() is called
        verify(pendingRegisterRepository).delete(any());
    }

    @Test
    @DisplayName("Verify OTP success")
    void verifyOtp_Success(){
        PendingRegister pendingRegister = new PendingRegister();

        // auto generated OTP and input OTP match
        pendingRegister.setAutoGeneratedOtp("36");
        String inputOtp = pendingRegister.getAutoGeneratedOtp();

        // OTP is not expired
        pendingRegister.setOtpExpiryTime(Instant.now().plusSeconds(36));

        // mock finding pending register by email
        when(pendingRegisterRepository.findByEmail(anyString())).thenReturn(pendingRegister);

        customerService.verifyOtp("test@email.com", inputOtp);

        // verify customerRepository.save() is called
        verify(customerRepository).save(any());

        // verify pendingRegisterRepository.delete() is called
        verify(pendingRegisterRepository).delete(any());
    }

    @Test
    @DisplayName("Verify OTP fails when auto generated OTP and input OTP do not match")
    void verifyOtp_Fail_AutoGeneratedOtpAndInputOtpNotMatch(){
        PendingRegister pendingRegister = new PendingRegister();

        // auto generated OTP and input OTP do not match
        pendingRegister.setAutoGeneratedOtp("36");
        String inputOtp = pendingRegister.getAutoGeneratedOtp() + "xxx";

        // mock finding pending register by email
        when(pendingRegisterRepository.findByEmail(anyString())).thenReturn(pendingRegister);

        var exception = assertThrows(RuntimeException.class, () -> customerService.verifyOtp("test@email.com", inputOtp));
        assertEquals("OTP không hợp lệ hoặc đã hết hạn", exception.getMessage());
    }

    @Test
    @DisplayName("Verify OTP fails when OTP has expired")
    void verifyOtp_Fail_OtpHasExpired(){
        PendingRegister pendingRegister = new PendingRegister();

        // auto generated OTP and input OTP match
        pendingRegister.setAutoGeneratedOtp("36");
        String inputOtp = pendingRegister.getAutoGeneratedOtp();

        // OTP is expired
        pendingRegister.setOtpExpiryTime(Instant.now().minusSeconds(36));

        // mock finding pending register by email
        when(pendingRegisterRepository.findByEmail(anyString())).thenReturn(pendingRegister);

        var exception = assertThrows(RuntimeException.class, () -> customerService.verifyOtp("test@email.com", inputOtp));
        assertEquals("OTP không hợp lệ hoặc đã hết hạn", exception.getMessage());
    }
}