package com.swp.project.service.product;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.repository.order.OrderItemRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.method.P;
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
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Map<String, Sort> SORT_OPTIONS = Map.of(
            "price-asc", Sort.by("price").ascending(),
            "price-desc", Sort.by("price").descending(),
            "name-asc", Sort.by("name").ascending(),
            "name-desc", Sort.by("name").descending(),
            "newest", Sort.by("id").descending(),
            "oldest", Sort.by("id").ascending(),
            "best-seller", Sort.by("best-seller"),
            "default", Sort.unsorted());

    public void saveProduct(Product product) {
        productRepository.save(product);

        eventPublisher
                .publishEvent(new ProductRelatedUpdateEvent(productRepository.findByName(product.getName()).getId()));
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

    public ShoppingCartItem getAllShoppingCartItemByCustomerIdAndProductId(String email, Long productId) {
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
        return loadProductsByCategoryWithSorting(categoryId, true, pageable);
    }

    /**
     * Lấy view sản phẩm bằng categoryId + sorting có paging
     * 
     * @param categoryId
     * @param enabled
     * @param sort
     * @param pageable
     * @return
     */
    private Page<ViewProductDto> loadProductsByCategoryWithSorting(Long categoryId, boolean enabled,
            Pageable pageable) {
        if (pageable.getSort().getOrderFor("best-seller") != null) {
            List<ViewProductDto> allProducts;
            if (categoryId == 0) {
                allProducts = productRepository.findAllViewProductDtoByEnabled(enabled);
            } else {
                allProducts = productRepository.findViewProductDtoByCategoryIdAndEnabled(categoryId, enabled);
            }

            List<ViewProductDto> sorted = allProducts.stream()
                    .sorted((p1, p2) -> Integer.compare(
                            orderRepository.getSoldQuantity(p2.getId()),
                            orderRepository.getSoldQuantity(p1.getId())))
                    .toList();

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), sorted.size());
            List<ViewProductDto> pageContent = sorted.subList(start, end);
            return new PageImpl<>(pageContent, pageable, sorted.size());
        }

        // Normal sorting for other cases
        if (categoryId == 0) {
            return productRepository.findAllViewProductDtoByEnabled(enabled, pageable);
        } else {
            return productRepository.findViewProductDtoByCategoryIdAndEnabled(categoryId, enabled, pageable);
        }
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

        //TODO: Best-seller sorting
        if ("best-seller".equals(sortBy)) {
            pageable = PageRequest.of(page, size,SORT_OPTIONS.get("default"));
        }

        // Normal sorting
        if (categoryId == 0) {
            return productRepository.findViewProductDtoByProductNameAndEnabled(keyword, true, pageable);
        }

        return productRepository.findViewProductDtoByProductNameAndCategoryIdAndEnabled(
                keyword, true, categoryId, pageable);
    }

    public Page<Product> GetAllProductList(int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        Page<Product> products= productRepository.findAll(pageable);
        products.forEach(product -> {
            int availableQuantity= getAvailableQuantity(product.getId());
            product.setTotalQuantity(availableQuantity);
        });
        return products;
    }

}
