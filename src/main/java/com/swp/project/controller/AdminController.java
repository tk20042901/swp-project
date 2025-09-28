package com.swp.project.controller;


import com.swp.project.dto.EditManagerDto;
import com.swp.project.dto.ManagerRegisterDto;

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
import org.springframework.web.bind.annotation.GetMapping;


@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ManagerService managerService;
    @GetMapping("")
    public String showAdminMainPage(Model model) {
        return "pages/admin/index";
    }
    
    @GetMapping("/create-manager")
    public String getCreateManagerPage(Model model) {
        model.addAttribute("managerRegisterDto", new ManagerRegisterDto());
        return "pages/admin/create-manager";
    }
    
    @GetMapping("/edit-manager/{id}")
    public String getEditManagerPage(
        @PathVariable Long id, 
        Model model, 
        RedirectAttributes redirectAttributes) {
        Manager manager = managerService.getManagerById(id);
        if (manager == null) {
            redirectAttributes.addFlashAttribute("failed", "Không tìm thấy quản lý.");
            return "redirect:/admin/manage-manager";
        }
        EditManagerDto editManagerDto = new EditManagerDto();
        editManagerDto.setId(manager.getId());
        editManagerDto.setEmail(manager.getEmail());
        editManagerDto.setFullname(manager.getFullname());
        editManagerDto.setBirthDate(manager.getBirthDate());
        editManagerDto.setCId(manager.getCid());
        editManagerDto.setCommuneWardCode(manager.getCommuneWard().getCode());
        editManagerDto.setSpecificAddress(manager.getSpecificAddress());
        editManagerDto.setStatus(manager.isEnabled());
        editManagerDto.setProvinceCityCode(manager.getCommuneWard().getProvinceCity().getCode());
        model.addAttribute("editManagerDto", editManagerDto);
        return "pages/admin/edit-manager";
    }

    @GetMapping("/manage-manager")
        public String showManageManagersPage(
            Model model) {
        List<ViewManagerDto> managers = managerService.getAllViewManager();
        model.addAttribute("managers", managers);
        return "pages/admin/manage-manager";
    }

    @PostMapping("/create-manager")
    public String createManager(
        @Valid @ModelAttribute ManagerRegisterDto managerRegisterDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("managerRegisterDto", managerRegisterDto);
            return "pages/admin/create-manager";
        }
        try {
            managerService.createManager(managerRegisterDto);
            redirectAttributes.addFlashAttribute("success", "Tạo quản lý thành công.");
        } 
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/admin/create-manager";
        }
        return "redirect:/admin/manage-manager";
    }

    @PostMapping("/edit-manager/{id}")
    public String editManager(@PathVariable Long id,
                              @Valid @ModelAttribute EditManagerDto editManagerDto,
                              RedirectAttributes redirectAttributes) {
        try {
            managerService.updateManager(id, editManagerDto);
            redirectAttributes.addFlashAttribute("success", "Cập nhật quản lý thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/admin/edit-manager/" + id;
        }
        return "redirect:/admin/manage-manager";
    }


}
