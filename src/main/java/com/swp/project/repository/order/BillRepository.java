package com.swp.project.repository.order;

import com.swp.project.entity.order.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill,Long> {
}
