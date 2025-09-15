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

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void initProducts() {

        List<Category> categories = categoryRepository.findAll();
        List<ProductUnit> units = productUnitRepository.findAll();

        if (categories.isEmpty() || units.isEmpty()) {
            throw new RuntimeException("Chưa có Category hoặc ProductUnit trong DB");
        }



        Object[][] productsData = {
                {"Táo đỏ", "Táo đỏ nhập khẩu, quả to đều, mọng nước, vị ngọt thanh, giàu vitamin C và chất xơ. Thích hợp ăn trực tiếp, làm salad hoặc nước ép.", 50000L, "Kg", "Trái cây nhập khẩu"},
                {"Cam vàng", "Cam vàng tươi ngon, mọng nước, ngọt tự nhiên và giàu vitamin C giúp tăng cường sức đề kháng. Phù hợp ăn trực tiếp hoặc làm nước ép.", 40000L, "Kg", "Trái cây Việt Nam"},
                {"Chuối sứ", "Chuối sứ chín tự nhiên, mềm, ngọt dịu, giàu kali và năng lượng. Thích hợp ăn trực tiếp, làm sinh tố hoặc bánh trái cây.", 30000L, "Kg", "Trái cây nhập khẩu"},
                {"Dâu tây", "Dâu tây Đà Lạt siêu ngọt, tươi ngon, mọng nước, giàu vitamin C và chất chống oxy hóa. Lý tưởng cho ăn trực tiếp, salad hoặc làm mứt.", 70000L, "Hộp", "Trái cây đang mùa"},
                {"Nho tím", "Nho tím không hạt, mọng, ngọt, giàu chất chống oxy hóa và vitamin. Thích hợp ăn trực tiếp, làm salad hoặc rượu vang tại nhà.", 80000L, "Hộp", "Trái cây nhập khẩu"},
                {"Xoài cát", "Xoài cát Hòa Lộc, vàng ươm, thơm ngọt, thịt mềm, giàu vitamin A và C. Phù hợp ăn trực tiếp hoặc làm sinh tố, kem trái cây.", 60000L, "Kg", "Trái cây Việt Nam"},
                {"Dưa leo", "Dưa leo giòn mát, mọng nước, giàu vitamin K và chất xơ. Thích hợp ăn sống, làm salad, hoặc làm nước detox thanh mát.", 15000L, "Kg", "Rau củ quả"},
                {"Bông cải xanh", "Bông cải xanh tươi, xanh mướt, giàu vitamin C và chất xơ, tốt cho hệ tiêu hóa. Dùng hấp, xào hoặc làm salad đều tốt.", 35000L, "Kg", "Rau củ quả"},
                {"Bắp cải tím", "Bắp cải tím giàu chất xơ, vitamin C và anthocyanin, tốt cho tiêu hóa và chống oxy hóa. Thích hợp làm salad, xào hoặc nấu canh.", 30000L, "Kg", "Rau củ quả"},
                {"Dứa (thơm)", "Dứa chín mọng nước, vàng ươm, ngọt thanh, giàu vitamin C và bromelain giúp tiêu hóa tốt. Thích hợp ăn trực tiếp hoặc làm nước ép.", 35000L, "Quả", "Trái cây Việt Nam"},
                {"Quýt hồng", "Quýt hồng ngọt lịm, mọng nước, giàu vitamin C và chất xơ. Phù hợp ăn trực tiếp hoặc làm nước ép giải khát.", 40000L, "Kg", "Trái cây Việt Nam"},
                {"Bưởi da xanh", "Bưởi da xanh đặc sản miền Tây, múi mọng, ngọt thanh, giàu vitamin C và chất chống oxy hóa. Ăn trực tiếp hoặc làm nước ép đều thơm ngon.", 60000L, "Quả", "Trái cây Việt Nam"},
                {"Chôm chôm", "Chôm chôm vỏ mỏng, ngọt nước, mọng, giàu vitamin C và chất chống oxy hóa. Phù hợp ăn trực tiếp hoặc làm món tráng miệng.", 50000L, "Kg", "Trái cây đang mùa"},
                {"Mận hậu", "Mận hậu chín mọng, ngọt thanh, giàu vitamin C và chất chống oxy hóa, tốt cho tim mạch và miễn dịch. Ăn trực tiếp hoặc làm mứt.", 55000L, "Kg", "Trái cây Việt Nam"},
                {"Lê Nam Phi", "Lê Nam Phi tươi ngon, giòn, ngọt dịu, giàu chất xơ, giúp tiêu hóa tốt và làm đẹp da. Ăn trực tiếp hoặc làm salad.", 60000L, "Kg", "Trái cây nhập khẩu"},
                {"Sapoche", "Sapoche chín mọng, vị ngọt đặc trưng, giàu vitamin và khoáng chất, hỗ trợ tiêu hóa và tăng cường năng lượng. Ăn trực tiếp hoặc làm sinh tố.", 75000L, "Kg", "Trái cây nhập khẩu"},
                {"Bơ sáp", "Bơ sáp chín mềm, béo ngậy, giàu chất béo lành mạnh, vitamin E và K. Thích hợp ăn trực tiếp, làm sinh tố hoặc salad.", 80000L, "Kg", "Trái cây nhập khẩu"},
                {"Thanh long đỏ", "Thanh long đỏ mọng nước, ngọt nhẹ, giàu vitamin C và chất chống oxy hóa. Ăn trực tiếp hoặc làm sinh tố giải khát.", 40000L, "Kg", "Trái cây Việt Nam"},
                {"Mít", "Mít chín vàng, ngọt thơm, giàu vitamin C, chất xơ và năng lượng. Ăn trực tiếp hoặc làm sinh tố, bánh trái cây.", 50000L, "Kg", "Trái cây Việt Nam"},
                {"Việt quất", "Việt quất tươi ngon, mọng, giàu chất chống oxy hóa và vitamin C, tốt cho mắt và tim mạch. Ăn trực tiếp hoặc làm salad và mứt.", 120000L, "Hộp", "Trái cây nhập khẩu"},
                {"Kiwi", "Kiwi nhập khẩu, chua ngọt vừa phải, giàu vitamin C, K và chất xơ, tốt cho tiêu hóa và miễn dịch. Ăn trực tiếp hoặc làm nước ép.", 90000L, "Kg", "Trái cây nhập khẩu"},
                {"Mơ", "Mơ chín mọng, vị ngọt nhẹ và hơi chua, giàu vitamin A, C, chất chống oxy hóa. Ăn trực tiếp hoặc làm mứt, nước ép.", 60000L, "Kg", "Trái cây Việt Nam"},
                {"Lựu", "Lựu đỏ mọng, ngọt thanh, giàu chất chống oxy hóa, vitamin C và kali. Tốt cho tim mạch và da, ăn trực tiếp hoặc làm nước ép.", 85000L, "Kg", "Trái cây nhập khẩu"},
                {"Ổi", "Ổi tươi ngon, giòn, mọng nước, giàu vitamin C và chất xơ, hỗ trợ tiêu hóa. Ăn trực tiếp hoặc làm nước ép, salad.", 30000L, "Kg", "Trái cây Việt Nam"},
                {"Măng cụt", "Măng cụt chín mọng, vị ngọt thanh, giàu chất chống oxy hóa, vitamin và khoáng chất. Ăn trực tiếp hoặc làm món tráng miệng.", 100000L, "Kg", "Trái cây nhập khẩu"},
                {"Hồng giòn", "Hồng giòn chín mọng, vị ngọt dịu, giàu chất xơ và vitamin, tốt cho hệ tiêu hóa. Ăn trực tiếp hoặc làm salad.", 70000L, "Kg", "Trái cây Việt Nam"},
                {"Dưa hấu", "Dưa hấu mọng nước, ngọt thanh, giàu lycopene và vitamin C. Phù hợp ăn trực tiếp, làm nước ép hoặc tráng miệng giải khát.", 25000L, "Kg", "Trái cây Việt Nam"},
                {"Đu đủ", "Đu đủ chín vàng, ngọt mát, giàu vitamin A, C và enzyme papain hỗ trợ tiêu hóa. Ăn trực tiếp hoặc làm sinh tố.", 30000L, "Kg", "Trái cây Việt Nam"},
                {"Mơ tây", "Mơ tây nhập khẩu, mọng nước, ngọt thanh, giàu chất chống oxy hóa và vitamin C. Ăn trực tiếp hoặc làm mứt, salad.", 95000L, "Kg", "Trái cây nhập khẩu"},
                {"Cherry", "Cherry đỏ tươi ngon, ngọt thanh, giàu chất chống oxy hóa và melatonin tự nhiên. Tốt cho giấc ngủ và chống viêm. Ăn trực tiếp hoặc làm mứt.", 150000L, "Hộp", "Trái cây nhập khẩu"},
                {"Nho xanh", "Nho xanh không hạt, giòn ngọt, mọng nước, giàu resveratrol và vitamin K. Thích hợp ăn trực tiếp, làm rượu vang hoặc salad.", 85000L, "Kg", "Trái cây nhập khẩu"},
                {"Táo xanh", "Táo xanh Granny Smith, vị chua ngọt đặc trưng, giàu pectin và chất xơ. Thích hợp ăn trực tiếp, làm bánh hoặc salad.", 55000L, "Kg", "Trái cây nhập khẩu"},
                {"Cam sành", "Cam sành Việt Nam, ngọt đậm đà, ít chua, giàu vitamin C và folate. Phù hợp vắt nước hoặc ăn trực tiếp.", 35000L, "Kg", "Trái cây Việt Nam"},
                {"Chanh vàng", "Chanh vàng tươi ngon, chua thanh, giàu vitamin C và citric acid. Dùng nấu ăn, làm nước chanh hoặc pha chế đồ uống.", 45000L, "Kg", "Trái cây Việt Nam"},
                {"Bưởi hồng", "Bưởi hồng ruột đỏ, ngọt thanh, ít đắng, giàu lycopene và vitamin C. Ăn trực tiếp hoặc làm salad trái cây.", 65000L, "Quả", "Trái cây Việt Nam"},
                {"Vải thiều", "Vải thiều Lục Ngạn, thịt trắng ngọt lịm, thơm đặc trưng, giàu vitamin C. Ăn trực tiếp hoặc làm nước ép, mứt.", 80000L, "Kg", "Trái cây đang mùa"},
                {"Nhãn", "Nhãn tươi ngọt thanh, thịt trắng trong, giàu vitamin C và glucose tự nhiên. Ăn trực tiếp hoặc nấu chè, làm mứt.", 60000L, "Kg", "Trái cây Việt Nam"},
                {"Long nhãn", "Long nhãn khô ngọt đậm đà, bổ máu, tốt cho thần kinh và giấc ngủ. Ăn trực tiếp, nấu chè hoặc hầm thuốc bổ.", 120000L, "Kg", "Trái cây khô"},
                {"Dưa gang", "Dưa gang ngọt mát, thơm nhẹ, giàu nước và vitamin A. Ăn trực tiếp làm tráng miệng hoặc làm sinh tố.", 30000L, "Kg", "Trái cây Việt Nam"},
                {"Mãng cầu", "Mãng cầu chín mềm, vị ngọt đặc biệt, giàu vitamin C và chất xơ. Ăn trực tiếp hoặc làm kem, sinh tố.", 70000L, "Kg", "Trái cây Việt Nam"},
                {"Cam canh", "Cam canh vỏ mỏng, múi to, ngọt đậm, ít hạt, giàu vitamin C. Vắt nước hoặc ăn trực tiếp đều ngon.", 38000L, "Kg", "Trái cây Việt Nam"},
                {"Dưa lưới", "Dưa lưới vàng ngọt, thơm đặc trưng, mọng nước, giàu beta-carotene. Ăn trực tiếp làm tráng miệng hoặc sinh tố.", 45000L, "Kg", "Trái cây nhập khẩu"},
                {"Táo tàu", "Táo tàu (jujube) ngọt thanh, giòn, giàu vitamin C và chất chống stress. Ăn trực tiếp hoặc sấy khô bảo quản.", 65000L, "Kg", "Trái cây nhập khẩu"}
        };

        for (Object[] data : productsData) {
            String name = (String) data[0];
            String description = (String) data[1];
            Long price = (Long) data[2];
            String unitName = (String) data[3];
            String categoryName = (String) data[4];
            String mainImageUrl = (String) data[5];

            if (productRepository.existsByName(name)) continue;

            ProductUnit unit = units.stream()
                    .filter(u -> u.getName().equals(unitName))
                    .findFirst()
                    .orElse(null);

            if (unit == null) continue;

            Category category = categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .findFirst()
                    .orElse(null);

            if (category == null) continue;

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .unit(unit)
                    .enabled(true)
                    .categories(List.of(category))
                    .main_image_url(mainImageUrl)
                    .build();

            productRepository.save(product);
        }
    }
}
