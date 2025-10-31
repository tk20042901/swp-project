package com.swp.project.service.order;

import java.time.LocalDateTime;
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
    private final OrderService orderService;

    public Page<Bill> getBills(int page, int size, String searchQuery, String sortCriteria, int k, String sortCriteriaInPage) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Bill> bills = billRepository.findAll()
        .stream()
        .sorted((o1, o2) -> {
            if (sortCriteria == null) return 0;
            return switch (sortCriteria) {
                case "id" -> o1.getId().compareTo(o2.getId());
                case "shopName" -> o1.getShopName().compareTo(o2.getShopName());
                case "customer" -> o1.getOrder().getCustomer().getFullName().compareTo(o2.getOrder().getCustomer().getFullName());
                case "paymentTime" -> o1.getPaymentTime().compareTo(o2.getPaymentTime());
                case "totalAmount" -> orderService.calculateTotalAmount(o1.getOrder())
                        .compareTo(orderService.calculateTotalAmount(o2.getOrder()));
                case "address" -> o1.getOrder().getAddressString().compareTo(o2.getOrder().getAddressString());
                default -> 0;
            };
        })
        .toList();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase();
            bills = bills.stream()
                    .filter(bill -> bill.getOrder().getCustomer().getFullName().toLowerCase().contains(lowerCaseQuery))
                    .toList();
        }

        int start = Math.min((page - 1) * size, bills.size());
        int end = Math.min(start + size, bills.size());
        List<Bill> pagedBills = bills.subList(start, end);

        pagedBills = pagedBills.stream()
        .sorted((o1, o2) -> {
            if (sortCriteriaInPage == null) return 0;
            return switch (sortCriteriaInPage) {
                case "id" -> k * o1.getId().compareTo(o2.getId());
                case "shopName" -> k * o1.getShopName().compareTo(o2.getShopName());
                case "customer" -> k * o1.getOrder().getCustomer().getFullName().compareTo(o2.getOrder().getCustomer().getFullName());
                case "paymentTime" -> k * o1.getPaymentTime().compareTo(o2.getPaymentTime());
                case "totalAmount" -> k * orderService.calculateTotalAmount(o1.getOrder())
                        .compareTo(orderService.calculateTotalAmount(o2.getOrder()));
                case "address" -> k * o1.getOrder().getAddressString().compareTo(o2.getOrder().getAddressString());
                default -> 0;
            };
        })
        .toList();

        return new PageImpl<>(pagedBills, pageable, bills.size());
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id).orElse(null);
    }

    public String getPaidAt(Bill bill) {
        LocalDateTime paymentTime = bill.getPaymentTime();
        if (paymentTime == null) {
            return "N/A";
        }
        return "Ngày " + paymentTime.getDayOfMonth() + " tháng " + paymentTime.getMonthValue() + " năm " + paymentTime.getYear() +
                " lúc " + String.format("%02d", paymentTime.getHour()) + ":" + String.format("%02d", paymentTime.getMinute());
    }
}
