package com.swp.project.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class VectorStoreService {
    private final VectorStore vectorStore;

    public void saveToVectorStore(String content, String type) {
        Map<String, Object> metadata = Map.of("type", type);
        Document document = new Document(content, metadata);
        vectorStore.add(List.of(document));
    }

}
