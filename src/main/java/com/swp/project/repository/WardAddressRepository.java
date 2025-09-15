package com.swp.project.repository;

import com.swp.project.entity.WardAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WardAddressRepository extends JpaRepository<WardAddress,String> {
}
