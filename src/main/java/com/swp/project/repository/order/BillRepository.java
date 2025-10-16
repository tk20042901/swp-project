package com.swp.project.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swp.project.entity.order.Bill;

public interface BillRepository extends JpaRepository<Bill,Long> {

}
