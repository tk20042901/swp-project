package com.swp.project.entity.voucher;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CustomerVoucherId implements Serializable {
    private Long customerId;
    private Long voucherId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerVoucherId that)) return false;
        return Objects.equals(customerId, that.customerId) &&
                Objects.equals(voucherId, that.voucherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, voucherId);
    }
}
