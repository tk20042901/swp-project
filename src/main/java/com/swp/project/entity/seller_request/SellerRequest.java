package com.swp.project.entity.seller_request;

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
public class SellerRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityName;

    @Column(columnDefinition = "TEXT")
    private String oldContent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    private Seller seller;

    @ManyToOne(fetch = FetchType.EAGER)
    private SellerRequestType requestType;

    @ManyToOne(fetch = FetchType.EAGER)
    private SellerRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
