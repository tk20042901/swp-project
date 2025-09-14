package com.swp.project.repository.user;

import com.swp.project.entity.user.CustomerSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSupportRepository extends JpaRepository<CustomerSupport,Long> {

    boolean existsByEmail(String email);
}