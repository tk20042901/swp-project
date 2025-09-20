package com.swp.project.entity.seller_request_pending;

import com.swp.project.entity.user.Seller;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Column(columnDefinition = "TEXT")
    private String payload;

    @ManyToOne(fetch = FetchType.EAGER)
    private Seller seller;

    @ManyToOne(fetch = FetchType.EAGER)
    private SellerRequestPendingType requestType;

    @ManyToOne(fetch = FetchType.EAGER)
    private SellerRequestPendingStatusType status;

    private LocalDateTime createdAt;
}
