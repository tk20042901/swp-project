package com.swp.project.controller;

import com.swp.project.dto.AiMessageDto;
import com.swp.project.service.CustomerAiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class CustomerAiController {

    private final CustomerAiService customerAiService;

    @GetMapping("/ai")
    public String ask(Model model, HttpSession session) {
        model.addAttribute("conversationId", UUID.randomUUID().toString());
        session.removeAttribute("conversation");
        session.setAttribute("conversation", new ArrayList<AiMessageDto>());
        return "pages/customer/ai";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam String conversationId,
                      @RequestParam String q,
                      @RequestParam MultipartFile image,
                      HttpSession session,
                      Model model) {
        List<AiMessageDto> conversation = (List<AiMessageDto>) session.getAttribute("conversation");
        try {
            customerAiService.ask(conversationId, q, image, conversation);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("conversationId", conversationId);
        session.setAttribute("conversation", conversation);
        model.addAttribute("conversation", conversation);
        return "pages/customer/ai";
    }
}
