package com.swp.project.controller.user;

import java.util.ArrayList;

import com.swp.project.entity.user.CustomerSupport;
import com.swp.project.service.user.CustomerSupportService;
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
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.address.ProvinceCityRepository;
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
    private final CustomerSupportService customerSupportService;
    private final ProvinceCityRepository provinceCityRepository;
    private final CommuneWardRepository communeWardRepository;

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
            Model model,
            HttpSession session) {

        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        sellerService.findAll();
        session.setAttribute("list", sellerService.getResults());
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
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
                    break;
            }
        }

        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        sellerService.findByNameAndCid(queryName, queryCid);
        sellerService.sortBy((String) session.getAttribute("sortCriteria"), (Integer) session.getAttribute("k"));
        session.setAttribute("list", sellerService.getResults());
        model.addAttribute("queryName", queryName);
        model.addAttribute("queryCid", queryCid);

        return "pages/manager/manage-seller";
    }


    @GetMapping("manage-shipper")
    public String manageShipper(
            @RequestParam(value = "clickedButton", required = false) String clickedButton,
            @RequestParam(value = "subpageIndex", required = false) Integer subpageIndex,
            @RequestParam(value = "queryName", required = false) String queryName,
            @RequestParam(value = "queryCid", required = false) String queryCid,
            Model model,
            HttpSession session) {

        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
            shipperService.findAll();
            session.setAttribute("list", shipperService.getResults());
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
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
                    break;
            }
        }

        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        shipperService.findByNameAndCid(queryName, queryCid);
        shipperService.sortBy((String) session.getAttribute("sortCriteria"), (Integer) session.getAttribute("k"));
        session.setAttribute("list", shipperService.getResults());
        model.addAttribute("queryName", queryName);
        model.addAttribute("queryCid", queryCid);

        return "pages/manager/manage-shipper";
    }


    @GetMapping("manage-customer-support")
    public String manageCustomerSupport(
            @RequestParam(value = "clickedButton", required = false) String clickedButton,
            @RequestParam(value = "subpageIndex", required = false) Integer subpageIndex,
            @RequestParam(value = "queryName", required = false) String queryName,
            @RequestParam(value = "queryCid", required = false) String queryCid,
            Model model,
            HttpSession session) {

        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        customerSupportService.findAll();
        session.setAttribute("list", customerSupportService.getResults());
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
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
                    break;
            }
        }

        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        customerSupportService.findByNameAndCid(queryName, queryCid);
        customerSupportService.sortBy((String) session.getAttribute("sortCriteria"), (Integer) session.getAttribute("k"));
        session.setAttribute("list", customerSupportService.getResults());
        model.addAttribute("queryName", queryName);
        model.addAttribute("queryCid", queryCid);

        return "pages/manager/manage-customer-support";
    }

    @PostMapping("/manage-seller")
    public String manageSeller(
            @RequestParam("queryName") String queryName,
            @RequestParam("queryCid") String queryCid,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
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

        sellerService.findByNameAndCid(queryName, queryCid);
        sellerService.sortBy(session.getAttribute("sortCriteria").toString(), (int) session.getAttribute("k"));
        session.setAttribute("list", sellerService.getResults());
        redirectAttributes.addFlashAttribute("queryName", queryName);
        redirectAttributes.addFlashAttribute("queryCid", queryCid);
        redirectAttributes.addAttribute("queryName", queryName);
        redirectAttributes.addAttribute("queryCid", queryCid);
        return "redirect:/manager/manage-seller";
    }

    @PostMapping("/manage-shipper")
    public String manageShipper(
            @RequestParam("queryName") String queryName,
            @RequestParam("queryCid") String queryCid,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
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

        shipperService.findByNameAndCid(queryName, queryCid);
        shipperService.sortBy(session.getAttribute("sortCriteria").toString(), (int) session.getAttribute("k"));
        session.setAttribute("list", shipperService.getResults());
        redirectAttributes.addFlashAttribute("queryName", queryName);
        redirectAttributes.addFlashAttribute("queryCid", queryCid);
        redirectAttributes.addAttribute("queryName", queryName);
        redirectAttributes.addAttribute("queryCid", queryCid);
        return "redirect:/manager/manage-shipper";
    }

    @PostMapping("/manage-customer-support")
    public String manageCustomerSupport(
            @RequestParam("queryName") String queryName,
            @RequestParam("queryCid") String queryCid,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {
            CustomerSupport customerSupport = customerSupportService.getByEmail(email);

            boolean isEnabled = !customerSupport.isEnabled();
            customerSupport.setEnabled(isEnabled);
            customerSupportService.save(customerSupport);
            customerSupportService.setCustomerSupportStatus(customerSupport.getId(), isEnabled);

            redirectAttributes.addFlashAttribute("msg", (isEnabled ? "Mở khóa " : "Khóa ") + customerSupport.getName() + " thành công" );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        customerSupportService.findByNameAndCid(queryName, queryCid);
        customerSupportService.sortBy(session.getAttribute("sortCriteria").toString(), (int) session.getAttribute("k"));
        session.setAttribute("list", customerSupportService.getResults());
        redirectAttributes.addFlashAttribute("queryName", queryName);
        redirectAttributes.addFlashAttribute("queryCid", queryCid);
        redirectAttributes.addAttribute("queryName", queryName);
        redirectAttributes.addAttribute("queryCid", queryCid);
        return "redirect:/manager/manage-customer-support";
    }


    @GetMapping("/edit-staff")
    public String editStaff(@RequestParam(value = "clickedButton", required = false) String clickedButton, Model model,
            HttpSession session) {
        if (clickedButton != null && !clickedButton.isEmpty()) {
            model.addAttribute("provinces", provinceCityRepository.findAll());
            model.addAttribute("wards", new ArrayList<>());
            model.addAttribute("staffDto", new StaffDto());

            switch (clickedButton) {
                case "Seller":
                    session.setAttribute("newClassName", "Seller");
                    break;
                case "Shipper":
                    session.setAttribute("newClassName", "Shipper");
                    break;
                case "CustomerSupport":
                    session.setAttribute("newClassName", "CustomerSupport");
                    break;
            }
        }
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

        redirectAttributes.addFlashAttribute("staffDto", staffDto);
        redirectAttributes.addFlashAttribute("provinces", provinceCityRepository.findAll());
        redirectAttributes.addFlashAttribute("wards", communeWardRepository.findAllByProvinceCity(provinceCityRepository.getByCode(staffDto.getProvinceCity())));
        model.addAttribute("staffDto", staffDto);
        model.addAttribute("provinces", provinceCityRepository.findAll());
        model.addAttribute("wards", communeWardRepository.findAllByProvinceCity(provinceCityRepository.getByCode(staffDto.getProvinceCity())));

        String managerRedirectUrl = "";
        String managerForwardUrl = "";
        String editRedirectUrl = "redirect:/manager/edit-staff";
        String editForwardUrl = "pages/manager/edit-staff";

        switch (newClassName) {
            case "Seller":
                managerRedirectUrl = "redirect:/manager/manage-seller";
                managerForwardUrl = "pages/manager/manage-seller";
                break;
            case "Shipper":
                managerRedirectUrl = "redirect:/manager/manage-shipper";
                managerForwardUrl = "pages/manager/manage-shipper";
                break;
            case "CustomerSupport":
                managerRedirectUrl = "redirect:/manager/manage-customer-support";
                managerForwardUrl = "pages/manager/manage-customer-support";
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
                if (newClassName != null && !newClassName.isEmpty()) {

                    switch (newClassName) {
                        case "Seller":
                            try {
                                sellerService.add(staffDto);
                                sellerService.findAll();
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
                                shipperService.findAll();
                                shipperService.sortBy(session.getAttribute("sortCriteria").toString(),
                                        (int) session.getAttribute("k"));

                                session.setAttribute("list", shipperService.getResults());
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return editRedirectUrl;
                            }

                            break;
                        case "CustomerSupport":
                            try {
                                customerSupportService.add(staffDto);
                                customerSupportService.findAll();
                                customerSupportService.sortBy(session.getAttribute("sortCriteria").toString(),
                                        (int) session.getAttribute("k"));

                                session.setAttribute("list", customerSupportService.getResults());
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return editRedirectUrl;
                            }

                            break;
                    }
                    redirectAttributes.addFlashAttribute("msg",
                            "Thêm tài khoản " + staffDto.getEmail() + " thành công");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        switch (newClassName) {
            case "Seller":
            case "Shipper":
            case "CustomerSupport":
                return managerRedirectUrl;
            default:
                return "redirect:/manager/manage-seller";
        }
    }
}
