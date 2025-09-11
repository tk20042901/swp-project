package com.swp.project.repository.user;

import com.swp.project.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    String ADMIN_ROLE_NAME = "Admin";
    String MANAGER_ROLE_NAME = "Manager";
    String CUSTOMER_ROLE_NAME = "Customer";

    Role findByName(String name);
    boolean existsByName(String name);
}