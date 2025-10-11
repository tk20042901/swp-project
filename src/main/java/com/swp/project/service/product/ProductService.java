package com.swp.project.service.product;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.repository.order.OrderItemRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
import com.swp.project.entity.product.SubImage;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import com.swp.project.service.order.OrderStatusService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductBatchService productBatchService;
    private final OrderStatusService orderStatusService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final String TEMPORARY_PATH = "src/main/resources/static/images/temporary-products/";
    private static final String DISPLAY_PATH = "/images/temporary-products/";

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

    public void add(Product product) {
        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProduct));
    }

    public void update(Product product) {
        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProduct));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Transactional
    public void pickProductInProductBatch(Long productId, int quantity) {
        List<ProductBatch> productBatches = productBatchService.getByProductId(productId);
        productBatches.sort(Comparator.comparing(ProductBatch::getExpiredDate)
                .thenComparingInt(ProductBatch::getQuantity));
        for (ProductBatch productBatch : productBatches) {
            if (quantity <= 0)
                break;
            if (productBatch.getQuantity() >= quantity) {
                productBatch.setQuantity(productBatch.getQuantity() - quantity);
                quantity = 0;
            } else {
                quantity -= productBatch.getQuantity();
                productBatch.setQuantity(0);
            }
            productBatchService.update(productBatch);
        }
    }

    public int getAvailableQuantity(Long productId) {
        int productBatchQuantity = productBatchService.getByProductId(productId)
                .stream()
                .mapToInt(ProductBatch::getQuantity)
                .sum();

        int pendingPaymentQuantity = orderItemRepository
                .getByProduct_IdAndOrder_OrderStatus(productId, orderStatusService.getPendingPaymentStatus()).stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return productBatchQuantity - pendingPaymentQuantity;
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
        List<Product> relatedProducts = allProducts.stream()
                .filter(p -> isProductNameRelated(p.getName(), productName))
                .limit(limit)
                .toList();
        return relatedProducts;
    }

    public int getSoldQuantity(Long id) {
        return orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isDeliveredStatus(order))
                .flatMap(order -> order.getOrderItem().stream())
                .filter(item -> item.getProduct().getId().equals(id))
                .mapToInt(item -> item.getQuantity())
                .sum();
    }

    private boolean isProductNameRelated(String productName, String anotherName) {
        if (productName.equals("") || anotherName.equals(""))
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
        if (productName.equals("") || keyword.equals(""))
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

    public List<SubImage> getSubImageList(List<MultipartFile> extraImages,String productName, Product product) throws Exception {
        List<SubImage> subImages = new ArrayList<>();
        List<String> extraImagePaths = saveExtraImages(productName, extraImages);
        if (extraImagePaths != null) {
            for (String path : extraImagePaths) {
                SubImage subImage = new SubImage();
                subImage.setProduct(product);
                subImage.setSub_image_url(path);
                subImages.add(subImage);
            }
        }
        return subImages;
    }

    public List<String> saveExtraImages(String productName, List<MultipartFile> extraImages) throws Exception {
        if (extraImages == null || extraImages.size() != 3) {
            throw new IllegalArgumentException("Chỉ có 3 ảnh phụ");
        }
        String folderName = ProductService.toSlugName(productName);
        Path uploadDir = Paths.get(TEMPORARY_PATH + folderName);
        List<String> savedPaths = new ArrayList<>();
        try {
            Files.createDirectories(uploadDir);
            for (int i = 0; i < 3; i++) {
                MultipartFile file = extraImages.get(i);
                try (InputStream inputStream = file.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    String fileName = String.format("%s-%d.jpg", folderName, i + 1);
                    Path filePath = uploadDir.resolve(fileName);
                    ImageIO.write(image, "jpg", filePath.toFile());
                    savedPaths.add(DISPLAY_PATH+ folderName + "/" + fileName);
                }
            }
            return savedPaths;
        } catch (Exception e) {
            deleteDirectory(uploadDir);
            throw new Exception("Upload ảnh lỗi " + e.getMessage(), e);
        }
    }

    public String saveMainImage(String productName, MultipartFile file) throws Exception {
        String folderName = ProductService.toSlugName(productName);
        Path uploadDir = Paths.get(TEMPORARY_PATH + folderName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(uploadDir);
            BufferedImage image = ImageIO.read(inputStream);
            String fileName = folderName + ".jpg";
            Path filePath = uploadDir.resolve(fileName);
            ImageIO.write(image, "jpg", filePath.toFile());
            return DISPLAY_PATH + folderName + "/" + fileName;
        } catch (Exception e) {
            deleteDirectory(uploadDir);
            throw new Exception("Upload ảnh lỗi " + e.getMessage(), e);
        }
    }

    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception e) {
                            }
                        });
            }
        } catch (Exception e) {
        }
    }

    public void checkUniqueProductName(String name) throws Exception {
        Product existingProduct = productRepository.findByName(name);
        if (existingProduct != null) {
            throw new Exception("Tên sản phẩm đã tồn tại. Vui lòng chọn tên khác.");
        }
    }

}
