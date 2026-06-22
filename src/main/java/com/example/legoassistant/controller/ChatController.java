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
public class ChatController {

    private final LegoSetRepository legoSetRepository;
    private final LegoAiService legoAiService;
    private final ChatHistoryRepository chatHistoryRepository;

    public ChatController(LegoSetRepository legoSetRepository,
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

        // PRINDEM EROAREA AICI
        try {
            String answer = legoAiService.askAssistant(id, ask.getQuestion(), ask.getTone(), ask.getRole());
            redirectAttributes.addFlashAttribute("aiResponse", answer);
            redirectAttributes.addFlashAttribute("lastQuestion", ask.getQuestion());
        } catch (Exception e) {
            // Verificam daca este o eroare de limitare (429) sau alta eroare
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                errorMessage = "🤖 The AI model is temporarily overloaded (too many requests). Please wait a few seconds and try again.";
            } else if (e.getMessage() != null && e.getMessage().contains("401")) {
                errorMessage = "🔑 The OpenRouter API key is incorrect or missing.";
            } else {
                errorMessage = "⚠️ An unexpected error occurred while communicating with the AI. Please try again.";
            }

            // Trimitem mesajul custom catre frontend
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }

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

    @PostMapping("/{id}/delete")
    public String deleteSet(@PathVariable @Min(1) Long id, RedirectAttributes redirectAttributes) {
        // 1. Stergem intai istoricul de chat asociat acestui set (altfel crapa baza de date)
        chatHistoryRepository.deleteByLegoSet_Id(id);

        // 2. Stergem setul efectiv din baza de date (va sterge automat si BuildingSteps datorita cascade=ALL)
        legoSetRepository.deleteById(id);

        // 3. Trimitem un mesaj de succes catre pagina principala
        redirectAttributes.addFlashAttribute("message", "Setul LEGO a fost sters cu succes!");

        return "redirect:/"; // Ne intoarcem pe pagina principala
    }
}

