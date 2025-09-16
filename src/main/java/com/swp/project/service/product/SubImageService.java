package com.swp.project.service.product;

import com.swp.project.repository.product.SubImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SubImageService {

    private final SubImageRepository subImageRepository;

}