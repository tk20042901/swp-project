package com.swp.project.service.product;

import java.io.File;
import java.io.IOException;
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
import com.swp.project.listener.event.GeminiUpdateProductEvent;
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

    // Ví dụ hàm saveProduct sử dụng VectorUpdateEvent
    public void saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new GeminiUpdateProductEvent(savedProduct));
    }

    // Ví dụ hàm updateProduct sử dụng VectorUpdateEvent
    public void updateProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new GeminiUpdateProductEvent(savedProduct));
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
            productBatchService.updateProductBatch(productBatch);
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

    public static String getMainImageUrl(
            MultipartFile imageFile,
            String productFileName) throws IOException {
        if (imageFile.isEmpty()) {
            return null;
        }
        if (ImageIO.read(imageFile.getInputStream()) == null) {
            throw new IllegalArgumentException("File không phải là hình ảnh hợp lệ");
        }
        String fruitName = ProductService.toSlugName(productFileName);
        String uploadDir = TEMPORARY_PATH + fruitName + "/";

        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        String fileName = fruitName + ".jpg";
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "images/" + fruitName + "/" + fileName;
    }

    public static String getSubImageUrl(
            MultipartFile imageFile,
            String productFileName,
            int imageNumber) throws IOException {
        imageNumber = Math.abs(imageNumber);
        if (imageFile.isEmpty()) {
            return null;
        }
        if (ImageIO.read(imageFile.getInputStream()) == null) {
            throw new IllegalArgumentException("File không phải là hình ảnh hợp lệ");
        }
        String fruitName = ProductService.toSlugName(productFileName);
        String uploadDir = TEMPORARY_PATH + fruitName + "/";

        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        String fileName = fruitName + "-" + imageNumber + ".jpg";
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "images/" + fruitName + "/" + fileName;
    }

    public void checkUniqueProductName(String name) throws Exception {
        Product existingProduct = productRepository.findByName(name);
        if (existingProduct != null) {
            throw new Exception("Tên sản phẩm đã tồn tại. Vui lòng chọn tên khác.");
        }
    }

    public List<SubImage> getSubImageList(List<MultipartFile> extraImages, String productFileName, Product product)
            throws Exception {
        List<SubImage> images = new ArrayList<>();
        int imageNumber = 1;
        for (MultipartFile file : extraImages) {
            if (file.isEmpty()) {
                continue;
            }
            String url = getSubImageUrl(file, productFileName, imageNumber);
            SubImage img = new SubImage();
            img.setProduct(product);
            img.setSub_image_url(url);
            images.add(img);
            imageNumber++; 
        }
        return images;
    }

    

}
