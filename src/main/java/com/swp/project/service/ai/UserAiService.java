package com.swp.project.service.ai;

import com.swp.project.entity.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAiService {

    private final static String type = "user";

    private final static int topK = 69;

    private final static String systemPrompt = """
            You are a professional and helpful admin assistant. Your main purpose is to assist the admin with managing user information.
            Answer in plain text and in the same language as the user.
            """;

    private final static String queryPrompt = """
            Context information is below.
            
            ---------------------
            <context>
            ---------------------
            
            Given the context information, answer the query: <query>
            
            Follow these rules:
            1. Your knowledge is STRICTLY LIMITED to the context I provide.
            2. If the context is empty or does not contain the answer, you should answer like "I'm sorry, I don't have enough information to answer that question."
            3. NEVER mention the context or the information provided. Avoid phrases like "Based on the context..." or "According to the information...".
            """;

    private String vectorStoreContent(User user) {
        return "This document describes a user account. " +
                "The user's unique identifier is " + user.getId() + ". " +
                "Email address: " + user.getEmail() + ". " +
                "The account is " +
                (user.isEnabled() ? "active and enabled" : "disabled") + ". " +
                "The user has the role: " + user.getRole().getName() + ". ";
    }

    public void saveUserToVectorStore(User user) {
        vectorStoreService.saveToVectorStore(vectorStoreContent(user), type);
    }

    private final ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(36)
            .build();

    private final ChatClient chatClient;
    private final VectorStoreService vectorStoreService;

    public UserAiService(ChatModel chatModel,
                         VectorStore vectorStore,
                         VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;

        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);

        PromptTemplate pt = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .template(queryPrompt)
                .build();

        chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
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

    public List<Message> ask(String conversationId, String q) {
        chatClient.prompt(q)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        return chatMemory.get(conversationId);
    }
}