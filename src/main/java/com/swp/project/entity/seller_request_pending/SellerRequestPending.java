package com.swp.project.entity.seller_request_pending;

import com.swp.project.entity.user.Seller;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SellerRequestPending {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @Lob
    private String payload;

    @ManyToOne(fetch = FetchType.EAGER)
    private Seller createdBy;

    private String status;

    private Instant createdAt;
}
