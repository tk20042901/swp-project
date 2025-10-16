package com.swp.project.service.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swp.project.entity.order.Bill;
import com.swp.project.repository.order.BillRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BillService {

    private final BillRepository billRepository;
    // private final OrderStatusService orderStatusService;


    public Page<Bill> getBills(int page, int size, String sortCriteria, int k) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Bill> bills = billRepository.findAll()
        .stream()
        .sorted((o1, o2) -> {
            if (sortCriteria == null) return 0;
            return switch (sortCriteria) {
                case "id" -> k * o1.getId().compareTo(o2.getId());
                case "paymentTime" ->
                        k * o1.getOrder().getCurrentShippingStatus().getId().compareTo(o2.getOrder().getCurrentShippingStatus().getId());
                case "shopName" -> {
                    int result = o1.getShopName().compareTo(o2.getShopName());
                    if (result == 0) {
                        result = k * o1.getId().compareTo(o2.getId());
                    }
                    yield result;
                }
                default -> 0;
            };
        })
        .toList();
        int start = Math.min((page - 1) * size, bills.size());
        int end = Math.min(start + size, bills.size());
        List<Bill> pagedBills = bills.subList(start, end);
        return new PageImpl<>(pagedBills, pageable, bills.size());
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id).orElse(null);
    }
}
