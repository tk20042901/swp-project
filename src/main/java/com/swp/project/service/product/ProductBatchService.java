package com.swp.project.service.product;

import com.swp.project.repository.product.ProductBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service

public class ProductBatchService {


    private final ProductBatchRepository productBatchRepository;
}
