package com.swp.project.controller;

import com.swp.project.dto.*;
import com.swp.project.entity.user.Manager;
import com.swp.project.service.AddressService;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.user.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final OrderService orderService;
    private final AddressService addressService;

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
        } catch (RuntimeException e) {
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

    @GetMapping("/statistic-report")
    public String getAdminStatisticReport(Model model) {
        Long totalUnitSold = orderService.getUnitSold();
        Long revenueToday = orderService.getRevenueToday();
        Long revenueThisWeek = orderService.getRevenueThisWeek();
        Long revenueThisMonth = orderService.getRevenueThisMonth();
        double dailyPercentageChange = orderService.getDailyPercentageChange();
        double weeklyPercentageChange = orderService.getWeeklyPercentageChange();
        double monthlyPercentageChange = orderService.getMonthlyPercentageChange();
        model.addAttribute("totalUnitSold", totalUnitSold == null ? 0 : totalUnitSold);
        model.addAttribute("revenueToday", revenueToday == null ? 0 : revenueToday);
        model.addAttribute("revenueThisWeek", revenueThisWeek == null ? 0 : revenueThisWeek);
        model.addAttribute("revenueThisMonth", revenueThisMonth == null ? 0 : revenueThisMonth);
        model.addAttribute("dailyPercentageChange", dailyPercentageChange);
        model.addAttribute("weeklyPercentageChange", weeklyPercentageChange);
        model.addAttribute("monthlyPercentageChange", monthlyPercentageChange);
        return "pages/admin/statistic-report";
    }

    @GetMapping("/provinces")
    @ResponseBody
    public List<ProvinceDto> getAllProvinceCities() {
        return addressService.getAllProvinceCity().stream()
                .map(pc -> {
                    ProvinceDto dto = new ProvinceDto();
                    dto.setCode(pc.getCode());
                    dto.setName(pc.getName());
                    return dto;
                })
                .toList();
    }

    @GetMapping("/wards{provinceId}")
    @ResponseBody
    public List<WardDto> getAllSelectedWard(@RequestParam String provinceId) {
        return addressService.getAllCommuneWardByProvinceCityCode(provinceId).stream()
                .map(ward -> {
                    WardDto dto = new WardDto();
                    dto.setCode(ward.getCode());
                    dto.setName(ward.getName());
                    return dto;
                })
                .toList();
    }

    @GetMapping("/detail-report")
    public String getDetailReport(Model model){
        List<RevenueDto> daysReport = orderService.getDaysRevenue();
        List<RevenueDto> monthsReport = orderService.getMonthsRevenue();
        model.addAttribute("daysReport", daysReport);
        model.addAttribute("monthsReport", monthsReport);
        return "pages/manager/detail-report";

    }

    @GetMapping("/days/export-excel")
    public ResponseEntity<InputStreamResource> exportDaysRevenueToExcel() throws IOException {
        ByteArrayInputStream in = orderService.exportDaysRevenueToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=revenue-7-days.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/months/export-excel")
    public ResponseEntity<InputStreamResource> exportMonthsRevenueToExcel() throws IOException {
        ByteArrayInputStream in = orderService.exportMonthsRevenueToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=revenue-12-months.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
