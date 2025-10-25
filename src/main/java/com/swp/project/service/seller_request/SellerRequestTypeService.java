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

    public SellerRequestType getAddType() {
        return sellerRequestTypeRepository.findByName("Thêm mới");
    }

    public SellerRequestType getUpdateType() {
        return sellerRequestTypeRepository.findByName("Cập nhật");
    }

    public SellerRequestType getDeleteType() {
        SellerRequestType type = sellerRequestTypeRepository.findByName("Xóa bỏ");
        if(type == null) {
            type = SellerRequestType.builder().name("Xóa bỏ").build();
            sellerRequestTypeRepository.save(type);
        }
        return type;
    }

    public boolean isDeleteType(SellerRequest request) {
        return request.getRequestType().getName().equals("Xóa bỏ");
    }

    public boolean isAddType(SellerRequest request) {
        return request.getRequestType().getName().equals("Thêm mới");
    }

    public boolean isUpdateType(SellerRequest request) {
        return request.getRequestType().getName().equals("Cập nhật");
    }
}
