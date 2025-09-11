package com.swp.project.service.user;

import com.swp.project.config.AiConfig;
import com.swp.project.entity.User;
import com.swp.project.service.ai.VectorStoreService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAiService {

    private final static String type = "user";
    private final static int topK = 69;
    private final static String systemPrompt = """
            You are a professional and helpful admin assistant. Your ONLY purpose is to assist the admin with managing user information. Maintain this persona at all times.
            If the query is a simple greeting or small talk, respond with a simple, polite, and brief greeting. Do not explain why.
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

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final VectorStoreService vectorStoreService;

    public UserAiService(AiConfig aiConfig,
                         ChatMemory chatMemory,
                         VectorStoreService vectorStoreService) {
        this.chatMemory = chatMemory;
        this.vectorStoreService = vectorStoreService;
        chatClient = aiConfig.createChatClient(type, topK, systemPrompt, queryPrompt);
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