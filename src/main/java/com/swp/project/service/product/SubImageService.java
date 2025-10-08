package com.swp.project.service.product;


import org.springframework.stereotype.Service;

import com.swp.project.entity.product.SubImage;
import com.swp.project.repository.product.SubImageRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SubImageService {
    private final SubImageRepository subImageRepository;

    public SubImage save(SubImage subImage) {
        return subImageRepository.save(subImage);
    }
}
