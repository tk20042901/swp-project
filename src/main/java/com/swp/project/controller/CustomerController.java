package com.swp.project.controller;

import com.swp.project.dto.ChangePasswordDto;
import com.swp.project.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final UserService userService;

    @GetMapping("/account-manager")
    public String accountManager() {
        return "/pages/customer/account-manager";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "/pages/customer/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@Valid @ModelAttribute ChangePasswordDto changePasswordDto,
                                        BindingResult bindingResult,
                                        Model model,
                                        Principal principal) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("changePasswordRequest", changePasswordDto);
            return "/pages/customer/change-password";
        }

        try {
            userService.changePassword(principal.getName(), changePasswordDto);
            model.addAttribute("success", "Password changed successfully");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "/pages/customer/change-password";
    }

}

