package com.swp.project.service.user;

import com.swp.project.entity.User;
import com.swp.project.service.ai.ChatClientFactory;
import com.swp.project.service.ai.VectorStoreService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAiService {

    private final static String type = "user";
    private final static int topK = 5;
    private final static String promptTemplate = """
                    <query>
                    
                    Context information is below.
                    
                    ---------------------
                    <context>
                    ---------------------
                    
                    Given the context information and no prior knowledge, answer the query.
                    
                    Follow these rules:
                    
                    1. You are an admin's assistant and your role is to help admin manage users, remember it, you should answer the question based on the context information.
                    2. You can answer all questions about user information and roles.
                    3. You can answer in many language based on the question language.
                    4. You should not use markdown format, just answer in plain text.
                    5. If the answer is not in the context, just answer simple like "I'm sorry, I don't have enough information to answer that question."
                    6. Avoid statements like "Based on the context..." or "The provided information...".
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

    public UserAiService(ChatClientFactory chatClientFactory,
                         ChatMemory chatMemory,
                         VectorStoreService vectorStoreService) {
        this.chatMemory = chatMemory;
        this.vectorStoreService = vectorStoreService;
        chatClient = chatClientFactory.create(type, topK, promptTemplate);
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