package com.swp.project.service.seller_request;

import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.entity.seller_request.SellerRequestStatusType;
import com.swp.project.repository.seller_request.SellerRequestStatusTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SellerRequestStatusTypeService {

    private final SellerRequestStatusTypeRepository sellerRequestStatusTypeRepository;

    public SellerRequestStatusType getPendingStatusType() {
        return sellerRequestStatusTypeRepository.findByName("Đang Chờ Duyệt");
    }

    public SellerRequestStatusType getApprovedStatusType() {
        return sellerRequestStatusTypeRepository.findByName("Đã Duyệt");
    }

    public SellerRequestStatusType getRejectedStatusType() {
        return sellerRequestStatusTypeRepository.findByName("Đã Từ Chối");
    }

    public boolean isPendingStatusType(SellerRequest sellerRequest){
        return sellerRequest.getStatus().getName().equals("Đang Chờ Duyệt");
    }
}
