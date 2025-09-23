package com.swp.project.repository.seller_request;

import com.swp.project.entity.seller_request.SellerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRequestRepository extends JpaRepository<SellerRequest,Long> {
}
