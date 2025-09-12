package com.swp.project.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VectorStoreService {
    private final VectorStore vectorStore;

    public void saveToVectorStore(String content) {
        Document document = new Document(content, Collections.emptyMap());
        vectorStore.add(List.of(document));
    }

}
