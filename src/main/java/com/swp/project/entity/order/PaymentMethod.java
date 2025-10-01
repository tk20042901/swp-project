package com.swp.project.entity.order;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class PaymentMethod {
    @Id
    private String id;

    @Column(length = 100, nullable = false, unique = true)
    private String description;
}
