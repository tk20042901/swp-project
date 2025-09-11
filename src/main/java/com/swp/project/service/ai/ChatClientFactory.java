package com.swp.project.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatClientFactory {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    public ChatClient create(String type,
                             int topK,
                             String promptTemplate) {
        PromptTemplate pt = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .template(promptTemplate)
                .build();

        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);

        return chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        RetrievalAugmentationAdvisor.builder()
                                .queryTransformers(CompressionQueryTransformer.builder()
                                        .chatClientBuilder(chatClientBuilder.build().mutate())
                                        .build())
                                .documentRetriever(VectorStoreDocumentRetriever.builder()
                                        .topK(topK)
                                        .similarityThreshold(0.75)
                                        .vectorStore(vectorStore)
                                        .filterExpression(new FilterExpressionBuilder()
                                                .eq("type", type)
                                                .build())
                                        .build())
                                .queryAugmenter(ContextualQueryAugmenter.builder()
                                        .promptTemplate(pt)
                                        .build())
                                .build()
                )
                .build();
    }

}