package com.swp.project.initializer;


import com.swp.project.service.CustomerAiService;
import com.swp.project.service.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminService adminService;
    private final CustomerService customerService;
    private final ManagerService managerService;
    private final SellerService sellerService;
    private final ShipperService shipperService;
    private final CustomerSupportService customerSupportService;
    private final CustomerAiService customerAiService;


    @Override
    public void run(String... args) {
        adminService.initAdmin();
        managerService.initManager();
        sellerService.initSeller();
        shipperService.initShipper();
        customerSupportService.initCustomerSupport();
        customerService.initCustomer();

        //productService.getAllProducts().forEach(customerAiService::saveProductToVectorStore);
    }
}
