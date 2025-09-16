package com.swp.project.service.product;

import com.swp.project.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

}
