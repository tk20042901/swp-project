package com.swp.project.entity.complaint;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.user.CustomerSupport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class ComplaintForm {
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "orderId")
    private Order order;

    @ManyToOne(cascade = CascadeType.ALL)
    private ComplaintFormType complaintFormType;

    @ManyToOne(cascade = CascadeType.ALL)
    private CustomerSupport customerSupport;

    @Column(nullable = false)
    @Nationalized
    private String content;

    @Column(nullable = false, length = 100)
    @Nationalized
    private String contact;
}
