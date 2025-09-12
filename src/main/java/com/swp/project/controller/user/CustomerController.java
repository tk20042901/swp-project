package com.swp.project.controller.user;

import com.swp.project.dto.ChangePasswordDto;
import com.swp.project.service.ai.CustomerAiService;
import com.swp.project.service.user.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerAiService customerAiService;

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
            customerService.changePassword(principal.getName(), changePasswordDto);
            model.addAttribute("success", "Password changed successfully");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "/pages/customer/change-password";
    }


    @GetMapping("/ai")
    public String ask(Model model) {
        model.addAttribute("conversationId", UUID.randomUUID().toString());
        return "pages/customer/ai-assistant";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam String conversationId, @RequestParam String q, Model model) {
        try {
            model.addAttribute("conversationId", conversationId);
            model.addAttribute("conversation", customerAiService.ask(conversationId, q));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "pages/customer/ai-assistant";
    }

}

