package com.swp.project.controller.user;

import com.swp.project.entity.user.User;
import com.swp.project.repository.user.UserRepository;
import com.swp.project.service.user.SellerService;
import com.swp.project.service.user.ShipperService;
import com.swp.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

//    @GetMapping("manage-staff")
//    public String manageStaff(Model model) {
//        model.addAttribute("filteredResults", sellerService.getAllSellers());
//        return "pages/manager/manage-staff";
//    }

    @GetMapping("manage-staff")
    public String manageStaff(@RequestParam(value = "clickedButton", required = false) String clickedButton, Model model) {
        if (clickedButton == null || clickedButton.isEmpty()) {
            model.addAttribute("filteredResults", sellerService.getAllSellers());
        } else {
            switch (clickedButton) {
                case "seller":
                    model.addAttribute("filteredResults", sellerService.getAllSellers());
                    break;
                case "shipper":
                    model.addAttribute("filteredResults", shipperService.getAllShippers());
                    break;
                default:
                    model.addAttribute("filteredResults", sellerService.getAllSellers()); // Default case
            }
        }
        return "pages/manager/manage-staff";
    }


    @GetMapping("edit-staff/{email}")
    public String editStaff(@PathVariable("email") String email, Model model) {
        model.addAttribute("user", userService.findUserByEmail(email));
        return "pages/manager/edit-staff";
    }

    @PostMapping("edit-staff")
    public String editStaff(@ModelAttribute("user") User user,
                            @RequestParam("saveButton") String saveButton,
                            @RequestParam("deleteButton") String deleteButton,
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
        } else if (deleteButton != null) {
            userRepository.delete(userRepository.findByEmail(email));
            redirectAttributes.addFlashAttribute("msg", "Xóa " + email + " thành công");
        }
        return  "redirect:/manager/manage-staff";
    }
}
