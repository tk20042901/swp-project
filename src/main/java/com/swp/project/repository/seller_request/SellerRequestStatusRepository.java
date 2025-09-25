package com.swp.project.repository.seller_request;

import com.swp.project.entity.seller_request.SellerRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRequestStatusRepository extends JpaRepository<SellerRequestStatus, Integer> {
    SellerRequestStatus findByName(String name);
}
