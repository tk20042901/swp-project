package com.swp.project.repository.user;

import com.swp.project.entity.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer getByEmail(String email);

    public Customer findByEmail(String name);
}
