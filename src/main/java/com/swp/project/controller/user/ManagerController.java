package com.swp.project.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/manager")
public class ManagerController {
    @GetMapping("")
    public String index() {
        return "pages/manager/index";
    }
}
