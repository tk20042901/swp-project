package com.swp.project.service.seller_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.repository.seller_request.SellerRequestRepository;
import com.swp.project.service.product.ProductUnitService;
import com.swp.project.service.user.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SellerRequestService {

    private final ObjectMapper objectMapper;
    private final SellerRequestRepository sellerRequestRepository;
    private final SellerRequestTypeService sellerRequestTypeService;
    private final SellerRequestStatusService sellerRequestStatusService;
    private final SellerService sellerService;
    private final ProductUnitService productUnitService;

    public List<SellerRequest> getAllSellerRequest(){
        return sellerRequestRepository.findAll();
    }

    public SellerRequest getSellerRequestById(Long id) {
        return sellerRequestRepository.findById(id).orElse(null);
    }

    public <T> void saveAddRequest(T entity, String sellerEmail) throws JsonProcessingException {
        sellerRequestRepository.save(SellerRequest.builder()
                .entityName(entity.getClass().getSimpleName())
                .content(objectMapper.writeValueAsString(entity))
                .seller(sellerService.getSellerByEmail(sellerEmail))
                .requestType(sellerRequestTypeService.getAddType())
                .status(sellerRequestStatusService.getPendingStatus())
                .createdAt(LocalDateTime.now())
                .build());
    }

    public <T> void saveUpdateRequest(T oldEntity, T entity, String sellerEmail) throws JsonProcessingException {
        sellerRequestRepository.save(SellerRequest.builder()
                .entityName(entity.getClass().getSimpleName())
                .oldContent(objectMapper.writeValueAsString(oldEntity))
                .content(objectMapper.writeValueAsString(entity))
                .seller(sellerService.getSellerByEmail(sellerEmail))
                .requestType(sellerRequestTypeService.getUpdateType())
                .status(sellerRequestStatusService.getPendingStatus())
                .createdAt(LocalDateTime.now())
                .build());
    }

    public <T> T getEntityFromContent(String content, Class<T> entityClass) throws JsonProcessingException {
        return objectMapper.readValue(content, entityClass);
    }

    public void approveRequest(Long requestId) throws JsonProcessingException {
        SellerRequest sellerRequest = getSellerRequestById(requestId);
        sellerRequest.setStatus(sellerRequestStatusService.getApprovedStatus());
        sellerRequestRepository.save(sellerRequest);

        String requestTypeName = sellerRequest.getRequestType().getName();
        String requestContent = sellerRequest.getContent();
        String entityName = sellerRequest.getEntityName();
        if(requestTypeName.equals(sellerRequestTypeService.getAddType().getName())) {
            if(entityName.equals(ProductUnit.class.getSimpleName())) {
                executeAddProductUnitRequest(requestContent);
            }
        } else if(requestTypeName.equals(sellerRequestTypeService.getUpdateType().getName())) {
            if(entityName.equals(ProductUnit.class.getSimpleName())) {
                executeUpdateProductUnitRequest(requestContent);
            }
        }
    }

    public void rejectRequest(Long requestId){
        SellerRequest sellerRequest = getSellerRequestById(requestId);
        sellerRequest.setStatus(sellerRequestStatusService.getRejectedStatus());
        sellerRequestRepository.save(sellerRequest);
    }

    public void executeAddProductUnitRequest(String requestContent) throws JsonProcessingException {
        ProductUnit productUnit = objectMapper.readValue(requestContent, ProductUnit.class);
        productUnitService.add(productUnit);
    }

    public void executeUpdateProductUnitRequest(String requestContent) throws JsonProcessingException {
        ProductUnit productUnit = objectMapper.readValue(requestContent, ProductUnit.class);
        productUnitService.update(productUnit);
    }
}
