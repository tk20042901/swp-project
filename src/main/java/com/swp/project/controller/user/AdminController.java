package com.swp.project.controller.user;

import com.swp.project.dto.EditManagerDto;
import com.swp.project.dto.ManagerRegisterDto;
import com.swp.project.dto.RegisterDto;
import com.swp.project.dto.ViewManagerDto;
import com.swp.project.entity.user.Manager;
import com.swp.project.service.user.ManagerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String getCreateManagerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "pages/admin/create-manager";
    }
    
    @GetMapping("/edit-manager/{id}")
    public String getEditManagerPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Manager manager = managerService.getManagerById(id);
        if (manager == null) {
            redirectAttributes.addFlashAttribute("failed", "Manager not found.");
            return "redirect:/admin/manage-manager";
        }
        EditManagerDto editManagerDto = new EditManagerDto();
        editManagerDto.setId(manager.getId());
        editManagerDto.setEmail(manager.getEmail());
        model.addAttribute("editManagerDto", editManagerDto);
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
            Model model) {
        List<ViewManagerDto> managers = managerService.getAllViewManager();
        model.addAttribute("managers", managers);
        return "pages/admin/manage-manager";
    }

    @PostMapping("/manage-manager")
    public String createManager(
        @Valid @ModelAttribute ManagerRegisterDto registerDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "pages/admin/create-manager";
        }
        try {
            managerService.createManager(registerDto);
            redirectAttributes.addFlashAttribute("success", "Manager created successfully.");
        } 
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/admin/create-manager";
        }
        return "redirect:/admin/manage-manager";
    }

    @PostMapping("/manage-manager/{id}")
    public String editManager(@PathVariable Long id,
                              @Valid @ModelAttribute EditManagerDto editManagerDto,
                              RedirectAttributes redirectAttributes) {
        try {
            managerService.updateManager(id, editManagerDto);
            redirectAttributes.addFlashAttribute("success", "Manager updated successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/admin/edit-manager/" + id;
        }
        return "redirect:/admin/manage-manager";
    }


}
