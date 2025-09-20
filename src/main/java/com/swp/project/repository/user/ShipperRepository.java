package com.swp.project.repository.user;

import com.swp.project.entity.user.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper,Long> {
    Shipper findByEmail(String email);

    Shipper findByCid(String Cid);

    List<Shipper> findByFullnameContainsAndCidContains(String name, String cId);
}
