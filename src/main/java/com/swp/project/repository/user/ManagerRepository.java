package com.swp.project.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.project.entity.user.Manager;

@Repository
public interface ManagerRepository extends JpaRepository<Manager,Long> {

    Manager findByCid(String cId);

    Manager findByEmail(String email);
}
