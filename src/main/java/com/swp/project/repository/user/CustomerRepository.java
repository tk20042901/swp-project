package com.swp.project.repository.user;

import com.swp.project.entity.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
    Customer getByEmail(String email);

    Customer getCustomerByEmail(String email);
}
