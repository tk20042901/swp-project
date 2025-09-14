package com.swp.project.service.product;

import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.repository.product.CategoryRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.product.ProductUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductUnitRepository productUnitRepository;

    @Transactional
    public void initProducts() {

        List<Category> categories = categoryRepository.findAll();
        List<ProductUnit> units = productUnitRepository.findAll();


        Object[][] productsData = {
                {"Táo đỏ", "Táo tươi ngon nhập khẩu", 50000L, "Kg", "Trái cây nhập khẩu"},
                {"Cam vàng", "Cam ngọt nhiều vitamin C", 40000L, "Kg", "Trái cây Việt Nam"},
                {"Chuối sứ", "Chuối chín tự nhiên", 30000L, "Kg", "Trái cây nhập khẩu"},
                {"Dâu tây", "Dâu Đà Lạt siêu ngọt", 70000L, "Hộp", "Trái cây đang mùa"},
                {"Nho tím", "Nho tím không hạt", 80000L, "Kg", "Trái cây nhập khẩu"},
                {"Xoài cát", "Xoài cát Hòa Lộc", 60000L, "Kg", "Trái cây Việt Nam"}
        };


        for (Object[] data : productsData) {
            String name = (String) data[0];
            String description = (String) data[1];
            Long price = (Long) data[2];
            String unitName = (String) data[3];
            String categoryName = (String) data[4];

            if (productRepository.existsByName(name)) continue;


            ProductUnit unit = units.stream()
                    .filter(u -> u.getName().equals(unitName))
                    .findFirst()
                    .orElse(null);


            Category category = categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .findFirst()
                    .orElse(null);

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .unit(unit)
                    .enabled(true)
                    .categories(category != null ? List.of(category) : null)
                    .main_image_url("/images/" + name.replace(" ", "_").toLowerCase() + ".jpg")
                    .build();

            productRepository.save(product);
        }
    }

}
