package com.swp.project.controller.user;

import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.entity.user.User;
import com.swp.project.repository.user.UserRepository;
import com.swp.project.service.user.SellerService;
import com.swp.project.service.user.ShipperService;
import com.swp.project.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/manager")
public class ManagerController {
    @Autowired
    private SellerService sellerService;

    @Autowired
    private ShipperService shipperService;

    @GetMapping("")
    public String index() {
        return "pages/manager/index";
    }

    @GetMapping("manage-staff")
    public String manageStaff(@RequestParam(value = "clickedButton", required = false) String clickedButton, Model model, HttpSession session) {
        int k = 1;
        List<?> list = new ArrayList<>();

        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("list") == null) {
            session.setAttribute("list", new ArrayList<>());
        }

        k = (Integer) session.getAttribute("k");
        list = (List<?>) session.getAttribute("list");

        if (clickedButton == null || clickedButton.isEmpty()) {
            model.addAttribute("filteredResults", sellerService.getAllSellers());
            session.setAttribute("k", k);
            session.setAttribute("className", "Seller");
        } else {
            final int newK = -k;
            k = newK;

            switch (clickedButton) {
                case "seller":
                    list = sellerService.getAllSellers();
                    session.setAttribute("className", "Seller");
                    break;
                case "shipper":
                    list = shipperService.getAllShippers();
                    session.setAttribute("className", "Shipper");
                    break;
                case "id":
                    list.sort((o1, o2) -> {
                        User tempO1 = (User) o1;
                        User tempO2 = (User) o2;
                        return newK * tempO1.getId().compareTo(tempO2.getId());
                    });
                    break;
                case "username":
                    list.sort((o1, o2) -> {
                        User tempO1 = (User) o1;
                        User tempO2 = (User) o2;
                        return newK * tempO1.getUsername().compareTo(tempO2.getUsername());
                    });
                    break;
                case "enabled":
                    list.sort((o1, o2) -> {
                        User tempO1 = (User) o1;
                        User tempO2 = (User) o2;
                        int tempO1IsEnabled = tempO1.isEnabled() ? 1 : 0;
                        int tempO2IsEnabled = tempO2.isEnabled() ? 1 : 0;
                        return newK * (tempO1IsEnabled - tempO2IsEnabled);
                    });
                    break;
            }

            model.addAttribute("filteredResults", list);
        }
        session.setAttribute("list", list);
        session.setAttribute("k", k);
//        System.out.println("Chương trình đã chạy đến đây (get)");
        return "pages/manager/manage-staff";
    }


    @PostMapping("/manage-staff")
    public String manageStaff(  @RequestParam("className") String className,
                                @RequestParam("email") String email,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
//        System.out.println("Chương trình đã chạy đến đây (post)");

        boolean isEnabled = false;

        if (className != null && !className.isEmpty()) {
            if (className.equals("Seller")) {
                Seller seller = sellerService.getByEmail(email);

                System.out.println(seller.getEmail());

                isEnabled = !seller.isEnabled();
                seller.setEnabled(isEnabled);
                sellerService.save(seller);
                List<Seller> list = (List<Seller>) session.getAttribute("list");
                list.add(seller);

                sellerService.setSellerStatus(seller.getId(), isEnabled);


            } else if (className.equals("Shipper")) {
                Shipper shipper = shipperService.getByEmail(email);
                isEnabled = !shipper.isEnabled();
                shipper.setEnabled(isEnabled);
                shipperService.save(shipper);
                List<Shipper> list = (List<Shipper>) session.getAttribute("list");
                list.add(shipper);

                shipperService.setSellerStatus(shipper.getId(), isEnabled);
            }
            redirectAttributes.addFlashAttribute("msg", (isEnabled ? "Hữu hiệu hóa" : "Vô hiệu hóa") + email + " thành công");

        }
        return  "redirect:/manager/manage-staff";
    }


    @GetMapping("/edit-staff")
    public String editStaff(@RequestParam("clickedButton") String clickedButton, Model model, HttpSession  session) {
        if (clickedButton != null && !clickedButton.isEmpty()) {
            switch (clickedButton) {
                case "seller":
                    session.setAttribute("className", "Seller");
                    model.addAttribute("user", new Seller());
                    break;
                case "shipper":
                    session.setAttribute("className", "Shipper");
                    model.addAttribute("user", new Shipper());
                    break;
            }
        }
        return "pages/manager/edit-staff";
    }

    @PostMapping("/edit-staff")
    public String editStaff(@RequestParam("email") String email,
                            @RequestParam("password") String password,
                            @RequestParam(defaultValue = "false") String enabled,
                            @RequestParam("className") String className,
                            RedirectAttributes redirectAttributes,
                            HttpSession  session) {
        if (className != null && !className.isEmpty()) {
            switch (className) {
                case "Seller":
                    Seller seller = new Seller();
                    seller.setEmail(email);
                    seller.setPassword(password);
                    seller.setEnabled(enabled.equals("true"));
                    sellerService.save(seller);

                    List<Seller> list = (List<Seller>) session.getAttribute("list");
                    list.add(seller);
                    session.setAttribute("list", list);
                    break;
                case "Shipper":
                    Shipper shipper = new Shipper();
                    shipper.setEmail(email);
                    shipper.setPassword(password);
                    shipper.setEnabled(enabled.equals("true"));
                    shipperService.save(shipper);

                    List<Shipper> list2 = (List<Shipper>) session.getAttribute("list");
                    list2.add(shipper);
                    session.setAttribute("list", list2);
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", "Thêm tài khoản thành công");
        }
        return "redirect:/manager/manage-staff";
    }
}
