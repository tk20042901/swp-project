package com.swp.project.initializer;

import com.swp.project.service.user.AdminService;
import com.swp.project.service.user.CustomerService;
import com.swp.project.service.user.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminService adminService;
    private final CustomerService customerService;
    private final ManagerService managerService;

    @Override
    public void run(String... args) {
        adminService.initAdmin();
        customerService.initCustomer();
        managerService.initManager();
    }
}
