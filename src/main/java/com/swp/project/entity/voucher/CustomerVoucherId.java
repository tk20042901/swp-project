package com.swp.project.entity.voucher;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class CustomerVoucherId implements Serializable {
    private Long customerId;
    private Long voucherId;
}
