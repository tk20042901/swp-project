package com.swp.project.controller;

import com.swp.project.service.user.UserAiService;
import com.swp.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final UserAiService userAiService;

    @GetMapping("")
    public String showAdminMainPage() {
        return "pages/admin/index";
    }

    @GetMapping("/all-managers")
    public String showAllManagers(Model model,
                                  @PageableDefault Pageable pageable) {
        model.addAttribute("pages", userService.getAllManagers(pageable));
        return "pages/admin/all-managers";
    }

    @PostMapping("/enable-manager")
    public String enableUser(@RequestParam(defaultValue = "") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.setUserEnabledStatus(id, true);
            redirectAttributes.addFlashAttribute("success", "Users enabled successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
        }
        return "redirect:/admin/enable-users";
    }

    @PostMapping("/disable-manager")
    public String disableUser(@RequestParam(defaultValue = "") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.setUserEnabledStatus(id, false);
            redirectAttributes.addFlashAttribute("success", "User disabled successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
        }
        return "redirect:/admin/disable-users";
    }

    @GetMapping("/ai")
    public String ask(Model model) {
        model.addAttribute("conversationId", UUID.randomUUID().toString());
        return "pages/admin/ai-assistant";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam String conversationId, @RequestParam String q, Model model) {
        try {
            model.addAttribute("conversationId", conversationId);
            model.addAttribute("conversation", userAiService.ask(conversationId, q));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "pages/admin/ai-assistant";
    }
}
