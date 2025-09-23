package com.swp.project.repository.seller_request;

import com.swp.project.entity.seller_request.SellerRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRequestTypeRepository extends JpaRepository<SellerRequestType,Long> {
    SellerRequestType findByName(String name);
}
