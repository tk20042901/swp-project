package com.swp.project.repository.user;

import com.swp.project.entity.user.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {
    Seller findByEmail(String email);

    Seller findByCid(String Cid);

    List<Seller> findByFullnameContainsAndCidContains(String name, String cId);

}
