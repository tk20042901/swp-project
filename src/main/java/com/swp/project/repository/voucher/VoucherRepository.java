package com.swp.project.repository.voucher;

import com.swp.project.entity.voucher.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher,Long> {
}
