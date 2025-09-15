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

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("customerId")
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("voucherId")
    private Voucher voucher;

    @Column(nullable = false)
    private Instant expiredDate;
}
