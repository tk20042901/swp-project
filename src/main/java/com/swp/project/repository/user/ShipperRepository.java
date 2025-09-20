package com.swp.project.repository.user;

import com.swp.project.entity.user.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper,Long> {
    boolean existsByEmail(String email);

    Shipper findByEmail(String email);

    Shipper findBycId(String cId);

    List<Shipper> findByNameContainsAndCIdContains(String name, String cId);
}
