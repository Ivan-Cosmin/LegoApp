package com.example.legoassistant.controller;

import com.example.legoassistant.dto.AskRequest;
import com.example.legoassistant.model.LegoSet;
import com.example.legoassistant.repository.ChatHistoryRepository;
import com.example.legoassistant.repository.LegoSetRepository;
import com.example.legoassistant.service.LegoAiService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/set")
public class SetChatController {

    private final LegoSetRepository legoSetRepository;
    private final LegoAiService legoAiService;
    private final ChatHistoryRepository chatHistoryRepository;

    public SetChatController(LegoSetRepository legoSetRepository,
                             LegoAiService legoAiService,
                             ChatHistoryRepository chatHistoryRepository) {
        this.legoSetRepository = legoSetRepository;
        this.legoAiService = legoAiService;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @ModelAttribute("ask")
    public AskRequest askRequest() {
        return new AskRequest();
    }

    @GetMapping("/{id}")
    public String viewSet(@PathVariable @Min(1) Long id, Model model, HttpSession session) {
        LegoSet legoSet = legoSetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set Id:" + id));
        model.addAttribute("legoSet", legoSet);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;

        if (username != null && !"anonymousUser".equals(username)) {
            model.addAttribute("chatHistory", chatHistoryRepository
                    .findByLegoSet_IdAndUser_UsernameOrderByTimestampAsc(id, username));
        } else {
            model.addAttribute("chatHistory", chatHistoryRepository
                    .findByLegoSet_IdOrderByTimestampAsc(id));
        }

        String tone = (String) session.getAttribute("tone");
        String role = (String) session.getAttribute("role");
        model.addAttribute("selectedTone", tone != null ? tone : "friendly");
        model.addAttribute("selectedRole", role != null ? role : "assistant");

        return "details";
    }

    @PostMapping("/{id}/ask")
    public String askQuestion(@PathVariable @Min(1) Long id,
                              @Valid @ModelAttribute AskRequest ask,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid input. Please check your question/tone/role.");
            return "redirect:/set/" + id;
        }

        if (ask.getTone() != null) {
            session.setAttribute("tone", ask.getTone());
        }
        if (ask.getRole() != null) {
            session.setAttribute("role", ask.getRole());
        }

        String answer = legoAiService.askAssistant(id, ask.getQuestion(), ask.getTone(), ask.getRole());
        redirectAttributes.addFlashAttribute("aiResponse", answer);
        redirectAttributes.addFlashAttribute("lastQuestion", ask.getQuestion());
        return "redirect:/set/" + id;
    }

    @PostMapping("/{id}/chat/clear")
    public String clearChatHistory(@PathVariable @Min(1) Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;

        if (username == null || "anonymousUser".equals(username)) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to clear chat history.");
            return "redirect:/set/" + id;
        }

        long deleted = chatHistoryRepository.deleteByLegoSet_IdAndUser_Username(id, username);
        redirectAttributes.addFlashAttribute("message", "Cleared " + deleted + " chat messages.");
        return "redirect:/set/" + id;
    }
}

