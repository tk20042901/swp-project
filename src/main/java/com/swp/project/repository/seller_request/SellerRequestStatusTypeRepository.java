package com.swp.project.repository.seller_request;

import com.swp.project.entity.seller_request.SellerRequestStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRequestStatusTypeRepository extends JpaRepository<SellerRequestStatusType, Integer> {
    SellerRequestStatusType findByName(String name);
}
