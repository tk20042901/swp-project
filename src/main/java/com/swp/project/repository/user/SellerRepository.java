package com.swp.project.repository.user;

import com.swp.project.entity.user.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {
    boolean existsByEmail(String email);

    Seller findByEmail(String email);

    Seller findByCId(String cId);
}
