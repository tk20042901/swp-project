package com.swp.project.controller.user;

import com.swp.project.service.user.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ManagerService managerService;

    @GetMapping("")
    public String showAdminMainPage() {
        return "pages/admin/index";
    }

    @PostMapping("/enable-manager")
    public String enableUser(@RequestParam(defaultValue = "") Long id, RedirectAttributes redirectAttributes) {
        try {
            managerService.setManagerStatus(id, true);
            redirectAttributes.addFlashAttribute("success", "Users enabled successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
        }
        return "redirect:/admin/enable-users";
    }

    @PostMapping("/disable-manager")
    public String disableUser(@RequestParam(defaultValue = "") Long id, RedirectAttributes redirectAttributes) {
        try {
            managerService.setManagerStatus(id, false);
            redirectAttributes.addFlashAttribute("success", "User disabled successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
        }
        return "redirect:/admin/disable-users";
    }

}
