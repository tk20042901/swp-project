package com.swp.project.controller;

import com.swp.project.service.CustomerAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class AiController {

    private final CustomerAiService customerAiService;

    @GetMapping("/ai")
    public String ask(Model model) {
        model.addAttribute("conversationId", UUID.randomUUID().toString());
        return "pages/customer/ai";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam String conversationId,
                      @RequestParam String q,
                      @RequestParam MultipartFile image,
                      Model model) {
        try {
            customerAiService.ask(conversationId, q, image);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("conversation", customerAiService.getConversation(conversationId));
        return "pages/customer/ai";
    }
}
