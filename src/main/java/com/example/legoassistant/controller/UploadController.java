package com.example.legoassistant.controller;

import com.example.legoassistant.dto.UploadSetRequest;
import com.example.legoassistant.model.LegoSet;
import com.example.legoassistant.repository.LegoSetRepository;
import com.example.legoassistant.service.LegoAiService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UploadController {

    private final LegoSetRepository legoSetRepository;
    private final LegoAiService legoAiService;

    public UploadController(LegoSetRepository legoSetRepository, LegoAiService legoAiService) {
        this.legoSetRepository = legoSetRepository;
        this.legoAiService = legoAiService;
    }

    @ModelAttribute("upload")
    public UploadSetRequest uploadSetRequest() {
        return new UploadSetRequest();
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        // upload attribute provided by @ModelAttribute
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@Valid @ModelAttribute("upload") UploadSetRequest upload,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {

        MultipartFile file = upload.getFile();
        if (file == null || file.isEmpty()) {
            bindingResult.rejectValue("file", "file.empty", "File is required");
        }

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType != null && !(contentType.equals("application/pdf") || contentType.startsWith("text/"))) {
                bindingResult.rejectValue("file", "file.type", "Only PDF or text files are allowed");
            }
            if (file.getSize() > 20L * 1024 * 1024) {
                bindingResult.rejectValue("file", "file.size", "File too large (max 20MB)");
            }
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.upload", bindingResult);
            redirectAttributes.addFlashAttribute("upload", upload);
            redirectAttributes.addFlashAttribute("error", "Please fix the errors and try again.");
            return "redirect:/upload";
        }

        try {
            String originalFilename = file != null ? file.getOriginalFilename() : null;
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "manual";
            }

            LegoSet set = new LegoSet();
            set.setName(upload.getName());
            set.setTheme(upload.getTheme());
            set.setSetNumber(upload.getSetNumber());
            set.setDescription("Uploaded Manual: " + originalFilename);

            set = legoSetRepository.save(set);
            legoAiService.processAndIndexFile(set, file.getResource());

            redirectAttributes.addFlashAttribute("message", "Uploaded and Indexed Successfully!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload: " + e.getMessage());
            return "redirect:/upload";
        }
    }
}

