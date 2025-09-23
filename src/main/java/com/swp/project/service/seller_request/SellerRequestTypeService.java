package com.swp.project.service.seller_request;

import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.entity.seller_request.SellerRequestType;
import com.swp.project.repository.seller_request.SellerRequestTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SellerRequestTypeService {

    private final SellerRequestTypeRepository sellerRequestTypeRepository;

    public SellerRequestType getAddProductUnitType() {
        return sellerRequestTypeRepository.findByName("Thêm đơn vị sản phẩm mới");
    }

    public SellerRequestType getUpdateProductUnitType() {
        return sellerRequestTypeRepository.findByName("Cập nhật thông tin đơn vị sản phẩm");
    }

    public boolean isAddProductUnitType(SellerRequest request) {
        return request.getRequestType().getName().equals("Thêm đơn vị sản phẩm mới");
    }

    public boolean isUpdateProductUnitType(SellerRequest request) {
        return request.getRequestType().getName().equals("Cập nhật thông tin đơn vị sản phẩm");
    }
}
