package com.swp.project.repository.user;

import com.swp.project.entity.user.CustomerSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerSupportRepository extends JpaRepository<CustomerSupport,Long> {
    CustomerSupport findByCid(String Cid);

    CustomerSupport findByEmail(String email);

    List<CustomerSupport> findByFullnameContainsAndCidContains(String name, String cid);

}