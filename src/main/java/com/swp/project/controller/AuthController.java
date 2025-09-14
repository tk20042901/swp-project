package com.swp.project.controller;

import com.swp.project.dto.RegisterDto;
import com.swp.project.service.user.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class AuthController {

    private final CustomerService customerService;

    @Value("${recaptcha.site-key}")
    private String recaptchaSite;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute RegisterDto registerDto, BindingResult bindingResult,  RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("siteKey", recaptchaSite);
            return "/pages/auth/register";
        }
        try {
            customerService.register(registerDto);
            redirectAttributes.addFlashAttribute("email", registerDto.getEmail());
            return "redirect:/verify-otp";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@ModelAttribute("email") String email,
                                    Model model) {
        if(email == null || email.isEmpty()){
            return "redirect:/register";
        }
        model.addAttribute("email", email);
        return "/pages/auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            Model model) {
        try {
            customerService.verifyOtp(email, otp);
            return "redirect:/login?register_success";
        } catch (RuntimeException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "pages/auth/verify-otp";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("siteKey", recaptchaSite);
        return "/pages/auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            customerService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("success",
                     "Mật khẩu mới vừa được gửi tới " + email);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/forgot-password";
    }

    @GetMapping({"/"})
    public String home() {
        return "pages/customer/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "/pages/auth/access-denied";
    }
}