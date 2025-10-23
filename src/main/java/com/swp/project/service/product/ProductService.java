package com.swp.project.service.product;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import com.swp.project.dto.CreateProductDto;
import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.order.OrderItemRepository;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import com.swp.project.service.order.OrderStatusService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderStatusService orderStatusService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ImageService imageService;
    private final CategoryService categoryService;
    private static final Map<String, Sort> SORT_OPTIONS = Map.of(
            "price-asc", Sort.by("price").ascending(),
            "price-desc", Sort.by("price").descending(),
            "name-asc", Sort.by("name").ascending(),
            "name-desc", Sort.by("name").descending(),
            "newest", Sort.by("id").descending(),
            "oldest", Sort.by("id").ascending(),
            "best-seller", Sort.by("soldQuantity").descending(),
            "default", Sort.unsorted());

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getFirstEnabledProduct() {
        return productRepository.findFirstByEnabledOrderByIdAsc(true);
    }

    public Product add(Product product) {
        Product savedProduct = productRepository.save(product);
        Path oldDir = Path.of(ImageService.IMAGES_FINAL_PATH + ProductService.toSlugName(product.getName()));
        Path newDir = Path.of(ImageService.IMAGES_FINAL_PATH + savedProduct.getId());
        try {
            Files.move(oldDir, newDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        savedProduct.setMain_image_url(ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" +
                "1.jpg");
        savedProduct.getSub_images().get(0).setSub_image_url(
                ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" + "2.jpg");
        savedProduct.getSub_images().get(1).setSub_image_url(
                ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" + "3.jpg");
        savedProduct.getSub_images().get(2).setSub_image_url(
                ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" + "4.jpg");
        savedProduct = productRepository.save(savedProduct);

        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProduct));
        return savedProduct;
    }

    public void update(Product product) throws Exception {
        if (product.getMain_image_url() != null && product.getMain_image_url().contains("temp-")) {
            String newMainImageUrl = imageService.renameTempFileToFinalName(
                    ImageService.IMAGES_FINAL_PATH + product.getId(),
                    "temp-1.jpg");
            product.setMain_image_url(newMainImageUrl);
        }
        if (product.getSub_images() != null) {
            for (int i = 0; i < product.getSub_images().size(); i++) {
                SubImage subImage = product.getSub_images().get(i);
                if (subImage.getSub_image_url() != null && subImage.getSub_image_url().contains("temp-")) {
                    String newSubImageUrl = imageService.renameTempFileToFinalName(
                            ImageService.IMAGES_FINAL_PATH + product.getId(),
                            "temp-" + (i + 2) + ".jpg");
                    subImage.setSub_image_url(newSubImageUrl);
                }
            }
        }
        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProduct));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Transactional
    public void reduceProductQuantity(Long productId, double quantity) {
        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

    public double getAvailableQuantity(Long productId) {
        double pendingPaymentQuantity = orderItemRepository
                .getByProduct_IdAndOrder_OrderStatus(productId, orderStatusService.getPendingPaymentStatus()).stream()
                .mapToDouble(OrderItem::getQuantity)
                .sum();

        return getProductById(productId).getQuantity() - pendingPaymentQuantity;
    }

    public ShoppingCartItem getShoppingCartItemByCustomerEmailAndProductId(String email, Long productId) {
        return shoppingCartItemRepository.findByCustomer_EmailAndProduct_Id(email, productId);
    }

    /**
     * Lấy sản phẩm enable theo danh mục với phân trang và sắp xếp
     * 
     * @param categoryId ID danh mục
     * @param page       Số trang
     * @param size       Kích thước trang
     * @param sortBy     Loại sắp xếp
     * @return Trang sản phẩm
     */
    public Page<ViewProductDto> getViewProductsByCategoryWithPagingAndSorting(
            Long categoryId, int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(page, size, SORT_OPTIONS.getOrDefault(sortBy, Sort.unsorted()));
        if (categoryId == 0) {
            return productRepository.findAllViewProductDtoByEnabled(true, pageable);
        } else {
            return productRepository.findViewProductDtoByCategoryIdAndEnabled(categoryId, true, pageable);
        }
    }

    public Map<String, Page<ViewProductDto>> getHomepageProductsBatch(Long categoryId, int size) {
        Map<String, Page<ViewProductDto>> results = new HashMap<>();

        // Get products by category
        Page<ViewProductDto> productsByCategory = getViewProductsByCategoryWithPagingAndSorting(categoryId, 0, size,
                "default");
        results.put("productByCategory", productsByCategory);

        // Get newest products
        Page<ViewProductDto> newestProducts = getViewProductsByCategoryWithPagingAndSorting(0L, 0, size, "newest");
        results.put("newestProducts", newestProducts);

        // Get most sold products
        Page<ViewProductDto> mostSoldProducts = getViewProductsByCategoryWithPagingAndSorting(0L, 0, size,
                "best-seller");
        results.put("mostSoldProducts", mostSoldProducts);

        return results;
    }

    public List<Product> getRelatedProducts(Long id, int limit) {
        Product product = getProductById(id);
        if (product == null) {
            return List.of();
        }
        String productName = product.getName();
        List<Product> allProducts = productRepository.findAllByEnabled(true).stream()
                .filter(p -> !p.getId().equals(id))
                .toList();
        return allProducts.stream()
                .filter(p -> isProductNameRelated(p.getName(), productName))
                .limit(limit)
                .toList();
    }

    public double getSoldQuantity(Long id) {
        return orderRepository.findAll().stream()
                .filter(order -> (orderStatusService.isProcessingStatus(order) ||
                        orderStatusService.isShippingStatus(order) ||
                        orderStatusService.isDeliveredStatus(order)))
                .flatMap(order -> order.getOrderItem().stream())
                .filter(item -> item.getProduct().getId().equals(id))
                .mapToDouble(OrderItem::getQuantity)
                .sum();
    }

    private boolean isProductNameRelated(String productName, String anotherName) {
        if (productName.isEmpty() || anotherName.isEmpty())
            return false;
        String[] keywords = anotherName.split(" ");
        for (String keyword : keywords) {
            if (isKeywordInProductName(productName, keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKeywordInProductName(String productName, String keyword) {
        if (productName.isEmpty() || keyword.isEmpty())
            return false;
        String[] splitedWords = productName.split(" ");
        for (String word : splitedWords) {
            if (word.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    public Page<ViewProductDto> searchViewProductDto(String keyword, Long categoryId, int page, int size,
            String sortBy) {

        Pageable pageable = PageRequest.of(page, size, SORT_OPTIONS.get(sortBy));
        if (categoryId == 0) {
            return productRepository.findViewProductDtoByProductNameAndEnabled(keyword, true, pageable);
        }

        return productRepository.findViewProductDtoByProductNameAndCategoryIdAndEnabled(
                keyword, true, categoryId, pageable);
    }

    public Page<Product> GetAllProductList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    public Page<Product> searchProductForSeller(String name, Boolean enabled, Pageable pageable) {
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasEnabled = enabled != null;

        if (hasName && hasEnabled) {
            return productRepository.findByNameContainingIgnoreCaseAndEnabled(name, enabled, pageable);
        } else if (hasName) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasEnabled) {
            return productRepository.findByEnabled(enabled, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    public Product getLastProduct() {
        return productRepository.findTopByOrderByIdDesc();
    }

    public static String toSlugName(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        normalized = normalized.toLowerCase();

        normalized = normalized.replaceAll("[^a-z0-9]+", "-");

        normalized = normalized.replaceAll("^-|-$", "");

        return normalized;
    }

    public boolean checkUniqueProductName(String name) {
        String slugName = toSlugName(name);
        return productRepository
                .findAll()
                .stream()
                .anyMatch(p -> toSlugName(p.getName()).equals(slugName));
    }
    private void validateProductDto(CreateProductDto productDto, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldErrors().get(0);
            String message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            throw new RuntimeException(message);
        }
        if (checkUniqueProductName(productDto.getName())) {
            throw new Exception("Tên sản phẩm đã tồn tại");
        }
        if (productDto.getImage() == null || productDto.getImage().isEmpty()) {
            throw new Exception("Vui lòng tải lên ảnh chính cho sản phẩm");
        }
        for (MultipartFile file : productDto.getSubImages()) {
            if (file == null || file.isEmpty()) {
                throw new Exception("Vui lòng tải lên đủ 3 ảnh phụ cho sản phẩm");
            }
        }
    }

    public Product createProductForAddRequest(CreateProductDto productDto, BindingResult bindingResult) throws Exception {
        validateProductDto(productDto, bindingResult);
        productDto.setCategories(new ArrayList<>());
        for (Long catId : productDto.getCategoryIds()) {
            productDto.getCategories().add(categoryService.getCategoryById(catId));
        }
        String fileName = ProductService.toSlugName(productDto.getName());
        Product product = Product.builder()
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .unit(productDto.getUnit())
                .enabled(productDto.isEnabled())
                .categories(productDto.getCategories())
                .main_image_url(imageService.saveTemporaryImage(productDto.getImage(), fileName, "1.jpg"))
                .sub_images(new ArrayList<>())
                .quantity(productDto.getQuantity())
                .build();
        for (int i = 0; i < productDto.getSubImages().size(); i++) {
            MultipartFile subImageFile = productDto.getSubImages().get(i);
            String subImagePath = imageService.saveTemporaryImage(subImageFile, fileName, (i + 2) + ".jpg");
            SubImage subImage = SubImage.builder()
                    .product(product)
                    .sub_image_url(subImagePath)
                    .build();
            product.getSub_images().add(subImage);
        }
        return product;
    }
    
}
