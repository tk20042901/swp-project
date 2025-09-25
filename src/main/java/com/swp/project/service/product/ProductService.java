package com.swp.project.service.product;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;
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
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void saveProduct(Product product) {
        productRepository.save(product);

        eventPublisher.publishEvent(new ProductRelatedUpdateEvent
                (productRepository.findByName(product.getName()).getId()));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    public void pickProductInProductBatch(Long productId, int quantity){
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
        return productBatchService.getByProductId(productId)
                .stream()
                .mapToInt(ProductBatch::getQuantity)
                .sum();
    }

    public ShoppingCartItem getAllShoppingCartItemByCustomerIdAndProductId(String email, Long productId) {
        return shoppingCartItemRepository.findByCustomer_EmailAndProduct_Id(email, productId);
    }

    /**
     * Lấy tất cả sản phẩm đang được kích hoạt
     * 
     * @return Danh sách sản phẩm
     */
    public List<Product> getAllEnabledProducts() {
        return productRepository.findAll().stream()
                .filter(product -> product.isEnabled())
                .toList();
    }

    

    /**
     * Lấy tất cả sản phẩm enable với phân trang
     * 
     * @param page Số trang
     * @param size Kích thước trang
     * @return Trang sản phẩm
     */
    public Page<Product> getProductsWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Product> allProducts = getAllEnabledProducts();
        return convertListToPage(allProducts, pageable);
    }

    /**
     * Lấy tất cả sản phẩm theo danh mục
     * 
     * @param categoryId ID danh mục
     * @return Danh sách sản phẩm
     */
    private List<Product> getProductsByCategory(List<Product> products,Long categoryId) {
        return products.stream()
                .filter(product -> product.getCategories().stream()
                        .anyMatch(category -> category.getId().equals(categoryId)))
                .toList();
    }

    /**
     * Lấy sản phẩm enable theo danh mục với phân trang
     * 
     * @param categoryId ID danh mục
     * @param page       Số trang
     * @param size       Kích thước trang
     * @return Trang sản phẩm
     */
    public Page<Product> getProductsByCategoryWithPaging(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Product> allProducts = getProductsByCategory(getAllEnabledProducts(), categoryId);
        Page<Product> productsPage = convertListToPage(allProducts, pageable);
        return productsPage;
    }

    /**
     * Đổi danh sách đã sort thành trang
     * 
     * @param products Danh sách sản phẩm đã được sắp xếp
     * @param pageable Đối tượng Pageable chứa số trang và kích thước
     * @return Trang sản phẩm
     */
    private Page<Product> convertListToPage(List<Product> products, Pageable pageable) {
        // Tính index bắt đầu
        int start = (int) pageable.getOffset();
        // Tính index kết thúc, sử dụng Math.min để tránh IndexOutOfBoundsException
        int end = Math.min((start + pageable.getPageSize()), products.size());
        List<Product> paginatedList = products.subList(start, end);
        return new PageImpl<>(paginatedList, pageable, products.size());
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa với phân trang
     * 
     * @param keyword Từ khóa tìm kiếm
     * @param page    Số trang
     * @param size    Kích thước trang
     * @return Trang sản phẩm
     */
    public Page<Product> searchProductsWithPaging(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return convertListToPage(searchProducts(getAllEnabledProducts(), keyword), pageable);
    }


    /**
     * Tìm kiếm sản phẩm từ danh sách theo từ khóa
     * 
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách sản phẩm đã lọc
     */
    private List<Product> searchProducts(List<Product> products,String keyword){
        List<Product> filteredProducts = products.stream()
                .filter(product -> normalizeString(product.getName())
                .contains(normalizeString(keyword)))
                .toList();
        return filteredProducts;
    }

    /**
     * Tìm kiếm sản phẩm enable rồi sắp xếp theo danh mục với phân trang
     * 
     * @param keyword    Từ khóa tìm kiếm
     * @param categoryId ID danh mục
     * @param page       Số trang
     * @param size       Kích thước trang
     * @return Trang sản phẩm
     */
    public Page<Product> searchProductsThenSortByCategoryWithPaging(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Product> searchProducts = searchProducts(getAllEnabledProducts(), keyword);
        List<Product> products  = getProductsByCategory(searchProducts, categoryId);
        return convertListToPage(products, pageable);
    }
    /**
     * Chuẩn hóa chuỗi bằng cách loại bỏ dấu và ký tự đặc biệt
     * 
     * @param input Chuỗi đầu vào
     * @return Chuỗi đã chuẩn hóa
     */
    private String normalizeString(String input) {
        if (input == null) {
            return null;
        }
        input = input.toLowerCase();
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", ""); // Remove diacritical marks
        return normalized.replaceAll("[^a-zA-Z0-9 ]", ""); // Remove non-alphanumeric characters except spaces
    }

    public List<Product> getRelatedProducts(Long id, int i) {
        Product product = getProductById(id);
        if (product == null) {
            return List.of();
        }
        return productRepository.findDistinctByCategoriesInAndIdNot(product.getCategories(), id, PageRequest.of(0, i));
    }

    public int getSoldQuantity(Long id) {
        int soldQuantity = orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isDeliveredStatus(order))
                .flatMap(order -> order.getOrderItem().stream())
                .filter(item -> item.getProduct().getId() == id)
                .mapToInt(item -> item.getQuantity())
                .sum();
        return soldQuantity;
    }
}
