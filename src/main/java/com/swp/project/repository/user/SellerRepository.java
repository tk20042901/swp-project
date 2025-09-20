package com.swp.project.repository.user;

import com.swp.project.entity.user.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {
    boolean existsByEmail(String email);

    Seller findByEmail(String email);

    Seller findBycId(String cId);

    List<Seller> findByNameContainsAndCIdContains(String name, String cId);
}
