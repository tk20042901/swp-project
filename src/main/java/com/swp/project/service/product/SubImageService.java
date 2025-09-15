package com.swp.project.service.product;


import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.product.SubImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@RequiredArgsConstructor
@Service
public class SubImageService {
    private final SubImageRepository subImageRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void initSubImages() {
        Product tao = productRepository.findByName("Táo đỏ");
        Product cam = productRepository.findByName("Cam vàng");
        Product chuoi = productRepository.findByName("Chuối sứ");
        Product dau = productRepository.findByName("Dâu tây");
        Product nho = productRepository.findByName("Nho tím");
        Product xoai = productRepository.findByName("Xoài cát");

        if (tao != null) {
            subImageRepository.save(SubImage.builder().product(tao).sub_image_url("/images/apple_1.jpg").build());
            subImageRepository.save(SubImage.builder().product(tao).sub_image_url("/images/apple_2.jpg").build());
            subImageRepository.save(SubImage.builder().product(tao).sub_image_url("/images/apple_3.jpg").build());
        }

        if (cam != null) {
            subImageRepository.save(SubImage.builder().product(cam).sub_image_url("/images/orange_1.jpg").build());
            subImageRepository.save(SubImage.builder().product(cam).sub_image_url("/images/orange_2.jpg").build());
            subImageRepository.save(SubImage.builder().product(cam).sub_image_url("/images/orange_3.jpg").build());
        }

        if (chuoi != null) {
            subImageRepository.save(SubImage.builder().product(chuoi).sub_image_url("/images/banana_1.jpg").build());
            subImageRepository.save(SubImage.builder().product(chuoi).sub_image_url("/images/banana_2.jpg").build());
        }

        if (dau != null) {
            subImageRepository.save(SubImage.builder().product(dau).sub_image_url("/images/strawberry_1.jpg").build());
            subImageRepository.save(SubImage.builder().product(dau).sub_image_url("/images/strawberry_2.jpg").build());
            subImageRepository.save(SubImage.builder().product(dau).sub_image_url("/images/strawberry_3.jpg").build());
            subImageRepository.save(SubImage.builder().product(dau).sub_image_url("/images/strawberry_4.jpg").build());
        }

        if (nho != null) {
            subImageRepository.save(SubImage.builder().product(nho).sub_image_url("/images/grape_1.jpg").build());
            subImageRepository.save(SubImage.builder().product(nho).sub_image_url("/images/grape_2.jpg").build());
            subImageRepository.save(SubImage.builder().product(nho).sub_image_url("/images/grape_3.jpg").build());
        }

        if (xoai != null) {
            subImageRepository.save(SubImage.builder().product(xoai).sub_image_url("/images/mango_1.jpg").build());
            subImageRepository.save(SubImage.builder().product(xoai).sub_image_url("/images/mango_2.jpg").build());
        }
    }
}