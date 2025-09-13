package com.swp.project.entity.complaint;

import com.swp.project.entity.order.Order;
import com.swp.project.entity.user.CustomerSupport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

    private String content;
    private String contact;
}
