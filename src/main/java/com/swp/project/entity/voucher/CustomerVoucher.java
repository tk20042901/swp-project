package com.swp.project.entity.voucher;


import com.swp.project.entity.user.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CustomerVoucher {
    @EmbeddedId
    private CustomerVoucherId customerVoucherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("customerId")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("voucherId")
    private Voucher voucher;


    private Instant expiredDate;
}
