package com.swp.project.controller.user;

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
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/manager")
public class ManagerController {
//    @Autowired
//    private final ManagerService managerService;
    @Autowired
    private SellerService sellerService;

    @Autowired
    private ShipperService shipperService;

    @Autowired
    private UserService userService;

//    @Autowired
//    private ShipperController shipperController;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public String index() {
        return "pages/manager/index";
    }

//    private int k = 1;
//
//    @Autowired
//    private List<?> list;

    @GetMapping("manage-staff")
    public String manageStaff(@RequestParam(value = "clickedButton", required = false) String clickedButton, Model model, HttpSession  session) {
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
        } else {
            final int newK = -k;
            k = newK;

            switch (clickedButton) {
                case "seller":
                    list = sellerService.getAllSellers();
                    break;
                case "shipper":
                    list = shipperService.getAllShippers();
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
        System.out.println("Chương trình đã chạy đến đây (get)");
        return "pages/manager/manage-staff";
    }


    @GetMapping("/edit-staff/{email}")
    public String editStaff(@PathVariable("email") String email, Model model) {
        model.addAttribute("user", userService.findUserByEmail(email));
        return "pages/manager/edit-staff";
    }

    @PostMapping("/edit-staff")
    public String editStaffPost(@ModelAttribute("user") User user,
                                @RequestParam("saveButton") String saveButton,
                                @RequestParam("email") String email,
                                RedirectAttributes redirectAttributes) {
        if (saveButton != null) {
            User tempUser = userService.findUserByEmail(user.getEmail());
            if (tempUser != null) {
                tempUser.setPassword(user.getPassword());
                tempUser.setEnabled(user.isEnabled());
                userRepository.save(tempUser);
                redirectAttributes.addFlashAttribute("msg", "Cập nhật thông tin cho " + tempUser.getName() + " thành công");

            }
        }
        System.out.println("Chương trình đã chạy đến đây");
        return  "redirect:/manager/manage-staff";
    }
}
