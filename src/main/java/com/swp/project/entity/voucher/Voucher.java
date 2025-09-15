package com.swp.project.entity.voucher;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double discount;

    @ManyToOne(fetch = FetchType.EAGER)
    private VoucherType voucherType;
}
