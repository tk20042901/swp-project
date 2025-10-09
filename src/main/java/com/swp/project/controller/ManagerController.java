package com.swp.project.controller;

import java.util.ArrayList;
import java.util.List;

import com.swp.project.service.order.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swp.project.dto.StaffDto;
import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.service.AddressService;
import com.swp.project.service.user.SellerService;
import com.swp.project.service.user.ShipperService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final SellerService sellerService;
    private final ShipperService shipperService;
    private final AddressService addressService;

    private final int numEachPage = 10;
    private final OrderService orderService;

    @GetMapping("")
    public String index() {
        return "pages/manager/index";
    }

    @GetMapping("manage-seller")
    public String manageSeller(
            @RequestParam(value = "clickedButton", required = false) String clickedButton,
            @RequestParam(value = "subpageIndex", required = false) Integer subpageIndex,
            @RequestParam(value = "queryName", required = false) String queryName,
            @RequestParam(value = "queryCid", required = false) String queryCid,
            HttpSession session) {
        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
        }
        if (session.getAttribute("numEachPage") == null) {
            session.setAttribute("numEachPage", numEachPage);
        }
        if (session.getAttribute("queryName") == null) {
            session.setAttribute("queryName", "");
        }
        if (session.getAttribute("queryCid") == null) {
            session.setAttribute("queryCid", "");
        }

        if (clickedButton != null && !clickedButton.isEmpty()) {
            switch (clickedButton) {
                case "id":
                case "email":
                case "fullname":
                case "cid":
                case "address":
                case "enabled":
                    session.setAttribute("sortCriteria", clickedButton);
                    int k = (int) session.getAttribute("k");
                    k = -k;
                    session.setAttribute("k", k);
                    break;
                case "search":
                    session.setAttribute("subpageIndex", 1);
                    session.setAttribute("queryName", queryName);
                    session.setAttribute("queryCid", queryCid);
                    break;
            }
        }
        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        sellerService.findByNameAndCid(session.getAttribute("queryName").toString(),
                                        session.getAttribute("queryCid").toString());
        sellerService.sortBy((String) session.getAttribute("sortCriteria"), (Integer) session.getAttribute("k"));
        session.setAttribute("list", sellerService.getResults());
        if (sellerService.getResults().size() - 1 < ((Integer) session.getAttribute("subpageIndex") - 1) * numEachPage) {
            session.setAttribute("subpageIndex", 1);
        }

        return "pages/manager/manage-seller";
    }


    @GetMapping("manage-shipper")
    public String manageShipper(
            @RequestParam(value = "clickedButton", required = false) String clickedButton,
            @RequestParam(value = "subpageIndex", required = false) Integer subpageIndex,
            @RequestParam(value = "queryName", required = false) String queryName,
            @RequestParam(value = "queryCid", required = false) String queryCid,
            HttpSession session) {
        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
        }
        if (session.getAttribute("numEachPage") == null) {
            session.setAttribute("numEachPage", numEachPage);
        }
        if (session.getAttribute("queryName") == null) {
            session.setAttribute("queryName", "");
        }
        if (session.getAttribute("queryCid") == null) {
            session.setAttribute("queryCid", "");
        }

        if (clickedButton != null && !clickedButton.isEmpty()) {
            switch (clickedButton) {
                case "id":
                case "email":
                case "fullname":
                case "cid":
                case "address":
                case "enabled":
                    session.setAttribute("sortCriteria", clickedButton);
                    int k = (int) session.getAttribute("k");
                    k = -k;
                    session.setAttribute("k", k);
                    break;
                case "search":
                    session.setAttribute("subpageIndex", 1);
                    session.setAttribute("queryName", queryName);
                    session.setAttribute("queryCid", queryCid);
                    break;
            }
        }
        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        shipperService.findByNameAndCid(session.getAttribute("queryName").toString(),
                                        session.getAttribute("queryCid").toString());
        shipperService.sortBy((String) session.getAttribute("sortCriteria"), (Integer) session.getAttribute("k"));
        session.setAttribute("list", shipperService.getResults());
        if (shipperService.getResults().size() - 1 < ((Integer) session.getAttribute("subpageIndex") - 1) * numEachPage) {
            session.setAttribute("subpageIndex", 1);
        }

        return "pages/manager/manage-shipper";
    }


    @PostMapping("/manage-seller")
    public String manageSeller(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            Seller seller = sellerService.getByEmail(email);

            boolean isEnabled = !seller.isEnabled();
            seller.setEnabled(isEnabled);
            sellerService.save(seller);
            sellerService.setSellerStatus(seller.getId(), isEnabled);

            redirectAttributes.addFlashAttribute("msg", (isEnabled ? "Mở khóa " : "Khóa ") + seller.getName() + " thành công" );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/manage-seller";
    }

    @PostMapping("/manage-shipper")
    public String manageShipper(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            Shipper shipper = shipperService.getByEmail(email);

            boolean isEnabled = !shipper.isEnabled();
            shipper.setEnabled(isEnabled);
            shipperService.save(shipper);
            shipperService.setShipperStatus(shipper.getId(), isEnabled);

            redirectAttributes.addFlashAttribute("msg", (isEnabled ? "Mở khóa " : "Khóa ") + shipper.getName() + " thành công" );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/manage-shipper";
    }



    @GetMapping("/edit-staff")
    public String editStaff(
            @RequestParam(value = "clickedButton", required = false) String clickedButton,
            @RequestParam(value = "email", required = false) String email,
            Model model,
            HttpSession session) {

        List<ProvinceCity> provinces = addressService.getAllProvinceCity();
        List<CommuneWard> wards = new ArrayList<>();
        StaffDto staffDto = (StaffDto) session.getAttribute("staffDto");

        if (clickedButton != null && !clickedButton.isEmpty()) {
            switch (clickedButton) {
                case "Seller":
                    session.setAttribute("newClassName", clickedButton);
                    if (email != null && !email.isEmpty()) {
                        Seller seller = sellerService.getByEmail(email);
                        staffDto = new StaffDto().parse(seller);
                    } else {
                        staffDto = new StaffDto();
                    }
                    break;
                case "Shipper":
                    session.setAttribute("newClassName", clickedButton);
                    if (email != null && !email.isEmpty()) {
                        Shipper shipper = shipperService.getByEmail(email);
                        staffDto = new StaffDto().parse(shipper);
                    }  else {
                        staffDto = new StaffDto();
                    }
                    break;
            }
        }
        if (staffDto.getProvinceCity() != null) {
            wards = addressService.getAllCommuneWardByProvinceCityCode(staffDto.getProvinceCity());
        }

        session.setAttribute("provinces", provinces);
        model.addAttribute("wards", wards);
        model.addAttribute("staffDto", staffDto);
        session.setAttribute("staffDto", staffDto);

        return "pages/manager/edit-staff";
    }

    @PostMapping("/edit-staff")
    public String editStaff(
            @Valid @ModelAttribute("staffDto") StaffDto staffDto,
            BindingResult bindingResult,
            @RequestParam("newClassName") String newClassName,
            @RequestParam(value = "submitButton", required = false) String submitButton,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {

        session.setAttribute("staffDto", staffDto);

        model.addAttribute("wards", addressService.getAllCommuneWardByProvinceCityCode(staffDto.getProvinceCity()));

        String managerRedirectUrl = "";
        String editRedirectUrl = "redirect:/manager/edit-staff";
        String editForwardUrl = "pages/manager/edit-staff";

        switch (newClassName) {
            case "Seller":
                managerRedirectUrl = "redirect:/manager/manage-seller";
                break;
            case "Shipper":
                managerRedirectUrl = "redirect:/manager/manage-shipper";
                break;
        }

        if (submitButton == null) {
            staffDto.setCommuneWard("");
            return editRedirectUrl;

        } else if (submitButton.equals("save")) {
            if (bindingResult.hasErrors()) {
                return editForwardUrl;
            }
            try {
                if (!newClassName.isEmpty()) {

                    switch (newClassName) {
                        case "Seller":
                            try {
                                sellerService.add(staffDto);
                                if (staffDto.getId() != 0) {
                                    sellerService.setSellerStatus(staffDto.getId(), staffDto.isEnabled());
                                }
                                sellerService.findByNameAndCid(
                                        session.getAttribute("queryName").toString(),
                                        session.getAttribute("queryCid").toString());
                                sellerService.sortBy(session.getAttribute("sortCriteria").toString(),
                                        (int) session.getAttribute("k"));

                                session.setAttribute("list", sellerService.getResults());
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return editRedirectUrl;
                            }
                            break;

                        case "Shipper":
                            try {
                                shipperService.add(staffDto);
                                if (staffDto.getId() != 0) {
                                    shipperService.setShipperStatus(staffDto.getId(), staffDto.isEnabled());
                                }
                                shipperService.findByNameAndCid(
                                        session.getAttribute("queryName").toString(),
                                        session.getAttribute("queryCid").toString());
                                shipperService.sortBy(session.getAttribute("sortCriteria").toString(),
                                        (int) session.getAttribute("k"));

                                session.setAttribute("list", shipperService.getResults());
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return editRedirectUrl;
                            }
                            break;
                    }
                    if (staffDto.getId() == 0) {
                        redirectAttributes.addFlashAttribute("msg",
                                "Thêm tài khoản " + staffDto.getEmail() + " thành công");
                    } else {
                        redirectAttributes.addFlashAttribute("msg",
                                "Sửa tài khoản " + staffDto.getEmail() + " thành công");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        switch (newClassName) {
            case "Seller":
            case "Shipper":
                return managerRedirectUrl;
            default:
                return "redirect:/manager/manage-seller";
        }
    }

    @GetMapping("/statistic-report")
        public String getManagerStastisticReport(Model model){
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
        return "pages/manager/statistic-report";

        }

}
