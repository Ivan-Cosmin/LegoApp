package com.example.legoassistant.controller;

import com.example.legoassistant.model.LegoSet;
import com.example.legoassistant.repository.LegoSetRepository;
import com.example.legoassistant.service.LegoAiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LegoWebController {

    private final LegoSetRepository legoSetRepository;
    private final LegoAiService legoAiService;

    public LegoWebController(LegoSetRepository legoSetRepository, LegoAiService legoAiService) {
        this.legoSetRepository = legoSetRepository;
        this.legoAiService = legoAiService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("legoSets", legoSetRepository.findAll());
        return "index";
    }

    @GetMapping("/set/{id}")
    public String viewSet(@PathVariable Long id, Model model) {
        LegoSet legoSet = legoSetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set Id:" + id));

        // Note: We REMOVED loadManualIntoMemory from here.
        // We assume it was loaded on startup or when the set was created.

        model.addAttribute("legoSet", legoSet);
        return "details";
    }

    @PostMapping("/set/{id}/ask")
    public String askQuestion(@PathVariable Long id, @RequestParam String question, RedirectAttributes redirectAttributes) {

        // Pass the ID to the service so it filters correctly
        String answer = legoAiService.askAssistant(id, question);

        redirectAttributes.addFlashAttribute("aiResponse", answer);
        redirectAttributes.addFlashAttribute("lastQuestion", question);

        return "redirect:/set/" + id;
    }
}