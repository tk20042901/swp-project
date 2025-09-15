package com.swp.project.initializer;


import com.swp.project.service.CustomerAiService;
import com.swp.project.service.product.*;
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
    private final ProductUnitService productUnitService;
    private final ProductBatchService productBatchService;
    private final CategoryService categoryService;
    private final SubImageService subImageService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final CustomerAiService customerAiService;


    @Override
    public void run(String... args) {
        adminService.initAdmin();
        customerService.initCustomer();
        managerService.initManager();
        sellerService.initSeller();
        shipperService.initShipper();
        customerSupportService.initCustomerSupport();
        productUnitService.initProductUnit();
        supplierService.initSupplier();
        categoryService.initCategory();
        productService.initProducts();
        productBatchService.initProductBatches();
        subImageService.initSubImages();

        productService.getAllProducts().forEach(customerAiService::saveProductToVectorStore);
    }
}
