package com.swp.project.service.product;


import com.swp.project.entity.product.Category;
import com.swp.project.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    @Transactional
    public void initCategory() {
        String [] categories = {"Trái cây nhập khẩu","Trái cây Việt Nam","Trái cây đang mùa","Trái cây đang được giảm giá","Rau củ quả"};
        for (String category: categories) {
            if (!categoryRepository.existsByName(category)) {
                categoryRepository.save(Category.builder()
                        .name(category)
                        .build()
                );
            }
        }

    }
}
