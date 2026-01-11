package com.example.legoassistant.controller;

import com.example.legoassistant.model.LegoSet;
import com.example.legoassistant.repository.LegoSetRepository;
import com.example.legoassistant.service.LegoAiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LegoWebController {

    private final LegoSetRepository legoSetRepository;
    private final LegoAiService legoAiService;

    public LegoWebController(LegoSetRepository legoSetRepository, LegoAiService legoAiService) {
        this.legoSetRepository = legoSetRepository;
        this.legoAiService = legoAiService;
    }

    // 1. Home Page
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("legoSets", legoSetRepository.findAll());
        return "index";
    }

    // 2. Show Upload Form
    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";
    }

    // 3. Handle File Upload
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam String name,
                                   @RequestParam String theme,
                                   @RequestParam Integer setNumber,
                                   @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            // A. Save the basic Set info to Database
            LegoSet set = new LegoSet();
            set.setName(name);
            set.setTheme(theme);
            set.setSetNumber(setNumber);
            set.setDescription("Uploaded Manual: " + file.getOriginalFilename());

            // (We save immediately to get the ID)
            set = legoSetRepository.save(set);

            // B. Process the File for AI
            // We convert MultipartFile to a Spring Resource to pass to Tika
            legoAiService.processAndIndexFile(set, file.getResource());

            redirectAttributes.addFlashAttribute("message", "Uploaded and Indexed Successfully!");
            return "redirect:/";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to upload: " + e.getMessage());
            return "redirect:/upload";
        }
    }

    // 4. View Details & Chat
    @GetMapping("/set/{id}")
    public String viewSet(@PathVariable Long id, Model model) {
        LegoSet legoSet = legoSetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set Id:" + id));
        model.addAttribute("legoSet", legoSet);
        return "details";
    }

    // 5. Ask Question
    @PostMapping("/set/{id}/ask")
    public String askQuestion(@PathVariable Long id, @RequestParam String question, RedirectAttributes redirectAttributes) {
        String answer = legoAiService.askAssistant(id, question);
        redirectAttributes.addFlashAttribute("aiResponse", answer);
        redirectAttributes.addFlashAttribute("lastQuestion", question);
        return "redirect:/set/" + id;
    }
}