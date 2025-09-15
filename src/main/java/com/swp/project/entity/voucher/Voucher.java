package com.swp.project.entity.voucher;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

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

    @Column(nullable = false,  unique = true, length = 50)
    @Nationalized
    private String name;

    @Column(nullable = false)
    private double discount;

    @ManyToOne(fetch = FetchType.EAGER)
    private VoucherType voucherType;
}
