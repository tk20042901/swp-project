package com.swp.project.initializer;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.swp.project.service.CustomerAiService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.AdminService;
import com.swp.project.service.user.CustomerService;
import com.swp.project.service.user.ManagerService;
import com.swp.project.service.user.SellerService;
import com.swp.project.service.user.ShipperService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminService adminService;
    private final CustomerService customerService;
    private final ManagerService managerService;
    private final SellerService sellerService;
    private final ShipperService shipperService;
    private final ProductService productService;
    private final CustomerAiService customerAiService;


    @Override
    public void run(String... args) {
        adminService.initAdmin();
        managerService.initManager();
        sellerService.initSeller();
        shipperService.initShipper();
        customerService.initCustomer();

//        productService.getAllProducts().forEach(customerAiService::saveProductToVectorStore);
    }
}
