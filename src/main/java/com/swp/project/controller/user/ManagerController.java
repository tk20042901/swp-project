package com.swp.project.controller.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private SellerService sellerService;

    @Autowired
    private ShipperService shipperService;
    
    @Autowired
    private ProvinceCityRepository provinceCityRepository;

    @Autowired
    private CommuneWardRepository communeWardRepository;

    @GetMapping("")
    public String index() {
        return "pages/manager/index";
    }

    @GetMapping("manage-staff")
    public String manageStaff(@RequestParam(value = "clickedButton", required = false) String clickedButton,
            @RequestParam(value = "subpageIndex", required = false) Integer subpageIndex,
            HttpSession session) {

        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("list") == null || ((List<?>) session.getAttribute("list")).isEmpty()) {
            sellerService.findAllSellers();
            session.setAttribute("list", sellerService.getResults());
        }
        if (session.getAttribute("className") == null) {
            session.setAttribute("className", "Seller");
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
        }

        if (clickedButton != null && !clickedButton.isEmpty()) {
            switch (clickedButton) {
                case "seller":
                    session.setAttribute("className", "Seller");
                    sellerService.findAllSellers();
                    session.setAttribute("list", sellerService.getResults());
                    session.setAttribute("subpageIndex", 1);
                    break;
                case "shipper":
                    session.setAttribute("className", "Shipper");
                    shipperService.findAllShippers();
                    session.setAttribute("list", shipperService.getResults());
                    session.setAttribute("subpageIndex", 1);
                    break;
                case "id":
                case "email":
                case "fullname":
                case "cId":
                case "address":
                case "enabled":
                    session.setAttribute("sortCriteria", clickedButton);
                    int k = (int) session.getAttribute("k");
                    k = -k;
                    session.setAttribute("k", k);
                    String className = session.getAttribute("className").toString();
                    if (className.equals("Seller")) {
                        sellerService.findAllSellers();
                        sellerService.sortBy(clickedButton, k);
                        session.setAttribute("list", sellerService.getResults());
                    } else if (className.equals("Shipper")) {
                        shipperService.findAllShippers();
                        shipperService.sortBy(clickedButton, k);
                        session.setAttribute("list", shipperService.getResults());
                    }
                    break;
            }
        }

        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        return "pages/manager/manage-staff";
    }

    @PostMapping("/manage-staff")
    public String manageStaff(@RequestParam("className") String className,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        boolean isEnabled = false;
        if (className != null && !className.isEmpty()) {
            session.setAttribute("className", className);
            if (className.equals("Seller")) {
                sellerService.findAllSellers();
                Seller seller = sellerService.getByEmail(email);

                isEnabled = !seller.isEnabled();
                seller.setEnabled(isEnabled);
                sellerService.save(seller);

                sellerService.findAllSellers();
                sellerService.sortBy(session.getAttribute("sortCriteria").toString(), (int) session.getAttribute("k"));
                session.setAttribute("list", sellerService.getResults());

                sellerService.setSellerStatus(seller.getId(), isEnabled);

            } else if (className.equals("Shipper")) {
                shipperService.findAllShippers();
                Shipper shipper = shipperService.getByEmail(email);

                isEnabled = !shipper.isEnabled();
                shipper.setEnabled(isEnabled);
                shipperService.save(shipper);

                shipperService.findAllShippers();
                shipperService.sortBy(session.getAttribute("sortCriteria").toString(), (int) session.getAttribute("k"));
                session.setAttribute("list", shipperService.getResults());

                shipperService.setShipperStatus(shipper.getId(), isEnabled);

            }
            redirectAttributes.addFlashAttribute("msg", (isEnabled ? "Mở khóa " : "Khóa ") + email + " thành công");

        }
        redirectAttributes.addFlashAttribute("list", session.getAttribute("list"));
        return "redirect:/manager/manage-staff";
    }

    @GetMapping("/edit-staff")
    public String editStaff(@RequestParam(value = "clickedButton", required = false) String clickedButton, Model model,
            HttpSession session) {
        if (clickedButton != null && !clickedButton.isEmpty()) {
            model.addAttribute("provinces", provinceCityRepository.findAll());
            model.addAttribute("wards", new ArrayList<>());
            switch (clickedButton) {
                case "seller":
                    session.setAttribute("newClassName", "Seller");
                    model.addAttribute("staffDto", new StaffDto());
                    break;
                case "shipper":
                    session.setAttribute("newClassName", "Shipper");
                    model.addAttribute("staffDto", new StaffDto());
                    break;
            }
        }
        return "pages/manager/edit-staff";
    }

    @PostMapping("/edit-staff")
    public String editStaff(
            @Valid @ModelAttribute("staffDto") StaffDto staffDto,
            BindingResult bindingResult,
            @RequestParam(value = "enabled", defaultValue = "false") String enabled,
            @RequestParam("newClassName") String newClassName,
            @RequestParam(value = "submitButton", required = false) String submitButton,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {
        redirectAttributes.addFlashAttribute("staffDto", staffDto);

        if (submitButton.equals("changeProvince")) {
            staffDto.setCommuneWard("");
            model.addAttribute("provinces", provinceCityRepository.findAll());
            model.addAttribute("wards", communeWardRepository.findAllByProvinceCity(provinceCityRepository.getByCode(staffDto.getProvinceCity())));
            return "pages/manager/edit-staff";

        } else if (submitButton.equals("save")) {
            if (bindingResult.hasErrors()) {
                return "pages/manager/edit-staff";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (newClassName != null && !newClassName.isEmpty()) {
                    switch (newClassName) {
                        case "Seller":
                            try {
                                staffDto.setEnabled(enabled);
                                sellerService.add(staffDto);
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return "redirect:/manager/edit-staff";
                            }

                            sellerService.findAllSellers();
                            sellerService.sortBy(session.getAttribute("sortCriteria").toString(),
                                    (int) session.getAttribute("k"));

                            session.setAttribute("list", sellerService.getResults());
                            break;
                        case "Shipper":
                            try {
                                shipperService.add(staffDto);
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return "redirect:/manager/edit-staff";
                            }

                            shipperService.findAllShippers();
                            shipperService.sortBy(session.getAttribute("sortCriteria").toString(),
                                    (int) session.getAttribute("k"));

                            session.setAttribute("list", shipperService.getResults());
                            break;
                    }
                    redirectAttributes.addFlashAttribute("msg",
                            "Thêm tài khoản " + staffDto.getEmail() + " thành công");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        redirectAttributes.addFlashAttribute("list", session.getAttribute("list"));
        return "redirect:/manager/manage-staff";
    }
}
