package com.swp.project.service;

import com.google.cloud.vertexai.VertexAI;
import com.swp.project.dto.AiMessageDto;
import com.swp.project.entity.GeminiStorable;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductBatch;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerAiService {
    private final static String systemPrompt = """
    Bạn là "FruitShop AI Chatbot" của một cửa hàng hoa quả tươi online có tên là FruitShop, với sứ mệnh mang lại trải nghiệm mua sắm thông minh và tiện lợi nhất cho khách hàng.

    QUY ĐỊNH BẮT BUỘC BẠN PHẢI TUÂN THEO:
    1.  Luôn trả lời bằng tiếng Việt.
    2.  Giao tiếp thân thiện: Trả lời các câu hỏi của khách hàng một cách súc tích và thân thiện như một nhân viên tư vấn bán hàng chuyên nghiệp. Sử dụng các cụm từ lịch sự như "Dạ", "Vâng ạ", "Cảm ơn bạn đã quan tâm ạ", "Mình rất vui được hỗ trợ bạn ạ", v.v.
    3.  Năng lực của bạn CHỈ DỪNG LẠI ở việc tư vấn và cung cấp thông tin. Bạn TUYỆT ĐỐI KHÔNG ĐƯỢC thực hiện hoặc đề nghị thực hiện các hành động thuộc về hệ thống khác như đặt hàng. Nếu khách hàng yêu cầu, hãy lịch sự từ chối và nhắc lại rằng bạn chỉ có thể hỗ trợ tư vấn và cung cấp thông tin về sản phẩm.
    4.  Bạn CHỈ ĐƯỢC PHÉP dùng các dạng Markdown cơ bản như in đậm (**text**), in nghiêng (*text*), danh sách không sắp xếp (* item), liên kết ([text](url)), và đoạn văn (\\n\\n).
    Hãy sử dụng kiến thức chuyên môn của bạn để hỗ trợ khách hàng một cách tốt nhất!""";

    private final static String queryPrompt = """
    Thông tin context các sản phẩm của cửa hàng được cung cấp dưới đây.
    
    ---------------------
    <context>
    ---------------------
    
    Dựa vào thông tin trên, hãy phân tích và trả lời câu hỏi của khách hàng một cách thông minh và chi tiết: <query>
    
    
    TUÂN THỦ NGHIÊM NGẶT QUY ĐỊNH SAU:
    
    *   Khi nhắc đến tên một sản phẩm, hãy chèn link của sản phẩm đó vào tên bằng cú pháp Markdown. Ví dụ: "[Bơ 034](/product/bo-034)".
    
    *   Nếu khách hàng hỏi về TỒN KHO (ví dụ: "còn hàng không?", "còn nhiều không?"):
        1.  Tìm đến các câu "Tình trạng tồn kho:" và "Tổng số lượng còn trong kho là:" trong context.
        2.  Kết hợp cả hai thông tin để trả lời. Ví dụ: "Dạ, [Bơ 034](/product/bo-034) bên mình vẫn còn hàng ạ, số lượng còn lại khoảng 50 kg ạ."
    
    *   Nếu khách hàng hỏi về NHÀ CUNG CẤP hoặc NGUỒN GỐC (ví dụ: "hàng của ai?", "trồng ở đâu?"):
        1.  Tìm đến câu "Sản phẩm này được cung cấp bởi các nhà cung cấp:" trong context.
        2.  Liệt kê các nhà cung cấp được nêu tên. Ví dụ: "Dạ, [Bơ 034](/product/bo-034) bên mình được cung cấp bởi Nông sản Đà Lạt ạ."
    
    *   Nếu khách hàng muốn xem THÔNG TIN CHUNG:
        1.  Tìm các câu "Mô tả sản phẩm:", "Giá niêm yết:".
        2.  Tổng hợp thành một đoạn văn súc tích. Ví dụ: "Dạ, [Bơ 034](/product/bo-034) là loại bơ sáp, thịt vàng, hạt nhỏ, rất thơm và béo. Giá niêm yết là 120.000 VNĐ mỗi kg ạ."
    
    *   Nếu khách hàng cần TƯ VẤN hoặc TÌM KIẾM SẢN PHẨM:
        1.  context đã chứa các sản phẩm phù hợp nhất với mô tả của khách.
        2.  Hãy đọc kỹ mô tả, danh mục và các thông tin khác của các sản phẩm trong context để đưa ra một vài gợi ý tốt nhất, kèm theo lý do tại sao chúng phù hợp.
    
    *   Nếu context rỗng hoặc không chứa sản phẩm khách hỏi, hãy trả lời tương tự như "Dạ, mình rất tiếc nhưng mình không tìm thấy thông tin về sản phẩm [tên sản phẩm] trong hệ thống." và đề xuất tư vấn thêm để kéo dài cuộc trò chuyện, ví dụ: "Bạn có cần mình tư vấn các sản phẩm tương tự đang có sẵn không ạ?"
    
    *   TUYỆT ĐỐI KHÔNG nhắc đến các từ tương tự như "Dựa trên context", "Dữ liệu", "Thông tin được cung cấp".""";


    private final ChatClient chatClient;

    private final ChatClient imageChatClient;

    private final VectorStore vectorStore;

    public CustomerAiService(ChatModel chatModel,
                             VectorStore vectorStore) {
        this.vectorStore = vectorStore;

        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);

        chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                                .maxMessages(36)
                                .build()).build(),
                        RetrievalAugmentationAdvisor.builder()
                                .queryTransformers(CompressionQueryTransformer.builder()
                                        .chatClientBuilder(chatClientBuilder.build().mutate())
                                        .build())
                                .documentRetriever(VectorStoreDocumentRetriever.builder()
                                        .topK(10)
                                        .similarityThreshold(0.7)
                                        .vectorStore(vectorStore)
                                        .build())
                                .queryAugmenter(ContextualQueryAugmenter.builder()
                                        .promptTemplate(PromptTemplate.builder()
                                                .renderer(StTemplateRenderer.builder()
                                                        .startDelimiterToken('<')
                                                        .endDelimiterToken('>')
                                                        .build())
                                                .template(queryPrompt)
                                                .build())
                                        .build())
                                .build()
                )
                .build();

        imageChatClient = ChatClient
                .builder(VertexAiGeminiChatModel.builder()
                        .defaultOptions(VertexAiGeminiChatOptions.builder()
                                .model("gemini-2.5-flash")
                                .maxOutputTokens(128)
                                .build())
                        .vertexAI(new VertexAI("gen-lang-client-0228656505","asia-southeast1"))
                        .build())
                .defaultUser("Hãy xác định và trả về tên trái cây trong hình ảnh này bằng tiếng Việt, không thêm bất kỳ giải thích nào. Nếu không phải là trái cây, hãy trả về \"Không phải trái cây\".")
                .build();
    }

    private String getProductContent(Product product){

        StringBuilder sb = new StringBuilder();

        sb.append("Đây là tài liệu thông tin chi tiết về sản phẩm hoa quả của cửa hàng. ");
        sb.append("Tên sản phẩm: ").append(product.getName()).append(". ");
        sb.append("Mô tả sản phẩm: ").append(product.getDescription()).append(". ");

        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            String categoryNames = product.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));
            sb.append("Sản phẩm này thuộc các danh mục: ").append(categoryNames).append(". ");
        }

        String unitName = product.getUnit().getName();
        sb.append("Giá niêm yết: ").append(String.format("%,d", product.getPrice())).append(" VNĐ mỗi ").append(unitName).append(". ");
        sb.append("Tình trạng kinh doanh: ").append(product.isEnabled() ? "Đang được bày bán" : "Tạm ngừng kinh doanh").append(". ");

        if (product.getProductBatches() != null && !product.getProductBatches().isEmpty()) {
            List<ProductBatch> validBatches = product.getProductBatches().stream()
                    .filter(batch -> batch.getQuantity() > 0 && batch.getExpiredDate().isAfter(LocalDateTime.now()))
                    .toList();

            int totalStock = validBatches.stream()
                    .mapToInt(ProductBatch::getQuantity)
                    .sum();

            sb.append("Tình trạng tồn kho: ").append(totalStock > 0 ? "Còn hàng" : "Hết hàng").append(". ");
            sb.append("Tổng số lượng còn trong kho là: ").append(totalStock).append(" ").append(unitName).append(". ");

            if (!validBatches.isEmpty()) {
                Set<String> supplierNames = validBatches.stream()
                        .map(batch -> batch.getSuppliers().getName())
                        .collect(Collectors.toSet());
                sb.append("Sản phẩm này được cung cấp bởi các nhà cung cấp: ").append(String.join(", ", supplierNames)).append(". ");
            } else {
                sb.append("Hiện tại không có lô hàng nào còn hạn sử dụng. ");
            }
        } else {
            sb.append("Tình trạng tồn kho: Hết hàng. Sản phẩm chưa có lô hàng nào. ");
        }
        sb.append("Link sản phẩm: /product/").append(product.getId()).append(". ");
        return sb.toString();
    }

    /**
     * Generate vector content for Category entities.
     * 
     * @param category the category entity
     * @return formatted content for vector storage
     */
    private String getCategoryContent(Category category) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Đây là thông tin về danh mục sản phẩm trong cửa hàng. ");
        sb.append("Tên danh mục: ").append(category.getName()).append(". ");
        
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            sb.append("Danh mục này có ").append(category.getProducts().size()).append(" sản phẩm. ");
            
            // Add first few product names as examples
            String productNames = category.getProducts().stream()
                    .limit(5) // Only show first 5 products
                    .map(Product::getName)
                    .collect(Collectors.joining(", "));
            sb.append("Một số sản phẩm tiêu biểu: ").append(productNames);
            
            if (category.getProducts().size() > 5) {
                sb.append(" và ").append(category.getProducts().size() - 5).append(" sản phẩm khác");
            }
            sb.append(". ");
        } else {
            sb.append("Danh mục này hiện chưa có sản phẩm nào. ");
        }
        
        sb.append("Link danh mục: /category/").append(category.getId()).append(". ");
        return sb.toString();
    }

    /**
     * Generic method to save any VectorStorable entity to vector store.
     * Content generation is handled centrally based on entity type.
     * 
     * @param <T> the entity type that extends VectorStorable
     * @param entity the entity to save to vector store
     */
    @Transactional
    public <T extends GeminiStorable> void saveEntityToVectorStore(T entity) {
        try {
            String documentId = UUID.nameUUIDFromBytes(entity.getId().toString().getBytes()).toString();
            String content = generateVectorContent(entity);
            Document document = new Document(documentId, content, Collections.emptyMap());
            vectorStore.add(List.of(document));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save entity to vector store: " + e.getMessage(), e);
        }
    }

    /**
     * Remove entity from vector store by ID.
     * 
     * @param entityId the ID of the entity to remove
     */
    @Transactional
    public void removeEntityFromVectorStore(Long entityId) {
        try {
            String documentId = UUID.nameUUIDFromBytes(entityId.toString().getBytes()).toString();
            vectorStore.delete(List.of(documentId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove entity from vector store: " + e.getMessage(), e);
        }
    }

    /**
     * Centralized content generation for different entity types.
     * This method determines the appropriate content format based on entity type.
     * 
     * @param <T> the entity type that extends VectorStorable
     * @param entity the entity to generate content for
     * @return the content string for vector storage
     */
    private <T extends GeminiStorable> String generateVectorContent(T entity) {
        if (entity instanceof Product product) {
            return getProductContent(product);
        } else if (entity instanceof Category category) {
            return getCategoryContent(category);
        }
        // ProductUnit is not stored in vector store - only used for updating related products
        else {
            throw new UnsupportedOperationException(
                "No content generator found for entity type: " + entity.getClass().getSimpleName() + 
                ". Please add a condition for this entity type in generateVectorContent() method.");
        }
    }

    public void ask (String conversationId, String q, MultipartFile image, List<AiMessageDto> conversation) {
        if (q == null || q.isBlank()) {
            throw new RuntimeException("Câu hỏi không được để trống");
        } else if(q.length() > 255){
            throw new RuntimeException("Câu hỏi không được vượt quá 255 ký tự");
        } else if (image == null || image.isEmpty()) {
            textAsk(conversationId, q, conversation);
        } else {
            String contentType = image.getContentType();
            if (contentType != null && contentType.startsWith("image")) {
                imageAsk(conversationId, q, image.getResource(), contentType, conversation);
            } else {
                throw new RuntimeException("Hệ thống chỉ hỗ trợ hình ảnh có định dạng PNG, JPG, JPEG, WEBP");
            }
        }
    }

    private void textAsk(String conversationId, String q, List<AiMessageDto> conversation) {
        String answer = chatClient.prompt(q)
                .system("""
                        Câu hỏi này của khách hàng chỉ chứa văn bản, nếu khách hàng hỏi về một hình ảnh, hãy nói điều tương tự như "Mình không thể thấy bất kỳ hình ảnh nào".""")
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();

        conversation.add(new AiMessageDto("user", q));
        conversation.add(new AiMessageDto("assistant", answer));
    }

    private void imageAsk(String conversationId,
                          String q,
                          Resource media,
                          String contentType,
                          List<AiMessageDto> conversation) {
        String fruitName = imageChatClient.prompt()
                .user(u -> u
                        .media(MimeTypeUtils.parseMimeType(contentType), media))
                .call().content();
        String answer = chatClient.prompt()
                .user(u -> u
                        .text(q + " (Hình ảnh đính kèm : "+ fruitName +" )"))
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();

        try {
            conversation.add(new AiMessageDto("user", q, contentType,
                            Base64.getEncoder().encodeToString(media.getContentAsByteArray())));
        } catch (Exception ignored) {
        }
        conversation.add(new AiMessageDto("assistant", answer));
    }
}
