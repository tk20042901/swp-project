package com.swp.project.service.order;

import com.swp.project.entity.order.PaymentMethod;
import com.swp.project.repository.order.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethod> getAllPaymentMethods(){
        return paymentMethodRepository.findAll();
    }

    public PaymentMethod getCodMethod(){
        return paymentMethodRepository.findById("COD").orElse(null);
    }

    public  PaymentMethod getQrMethod(){
        return paymentMethodRepository.findById("QR").orElse(null);
    }

    public boolean isCodMethod(PaymentMethod paymentMethod){
        return "COD".equals(paymentMethod.getId());
    }

    public boolean isQrMethod(PaymentMethod paymentMethod){
        return "QR".equals(paymentMethod.getId());
    }
}
