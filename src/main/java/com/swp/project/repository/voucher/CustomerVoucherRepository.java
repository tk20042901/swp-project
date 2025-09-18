package com.swp.project.repository.voucher;

import com.swp.project.entity.voucher.CustomerVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher,Long> {
}
