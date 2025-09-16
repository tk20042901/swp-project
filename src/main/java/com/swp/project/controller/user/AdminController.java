package com.swp.project.controller.user;

import com.swp.project.entity.user.Manager;
import com.swp.project.service.user.ManagerService;
import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    
    @GetMapping("/create-manager")
    public String getCreateManagerPage() {
        return "pages/admin/create-manager";
    }
    @GetMapping("/edit-manager/{id}")
    public String getEditManagerPage(@PathVariable Long id, Model model) {
        Manager manager = managerService.getManagerById(id);
        model.addAttribute("manager", manager);
        return "pages/admin/edit-manager";
    }
    @PostMapping("/configure-manager")
    public String configureUser(@RequestParam(defaultValue = "") Long id, RedirectAttributes redirectAttributes) {
        try {
            Manager manager = managerService.getManagerById(id);
            if (manager.isEnabled()) {
                // Logic to disable the manager
                managerService.setManagerStatus(id, false);
                redirectAttributes.addFlashAttribute("success", "User disabled successfully.");
            } else {
                // Logic to enable the manager
                managerService.setManagerStatus(id, true);
                redirectAttributes.addFlashAttribute("success", "User enabled successfully.");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
        }
        return "redirect:/admin/manage-manager";
    }

    @GetMapping("/manage-manager")
        public String showManageManagersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Page<Manager> managerPage = managerService.getAllManagers(page, size);
        model.addAttribute("managers", managerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", managerPage.getTotalPages());
        model.addAttribute("totalItems", managerPage.getTotalElements());

        return "pages/admin/manage-manager";
    }

    @PostMapping("/manage-manager")
    public String createManager(@RequestParam String email,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        try {
            Manager manager = new Manager();
            manager.setEmail(email);
            manager.setPassword(password);
            managerService.createManager(manager);
            redirectAttributes.addFlashAttribute("success", "Manager created successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
        }
        return "redirect:/admin/manage-manager";
    }

    @PostMapping("/manage-manager/{id}")
    public String editManager(@PathVariable Long id,
                              //@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              RedirectAttributes redirectAttributes) {
        try {
            Manager updatedManager = new Manager();
            //updatedManager.setUsername(username);
            updatedManager.setEmail(email);
            updatedManager.setPassword(password);
            managerService.updateManager(id, updatedManager);
            redirectAttributes.addFlashAttribute("success", "Manager updated successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/admin/edit-manager/" + id;
        }
        return "redirect:/admin/manage-manager";
    }


}
