package com.example.legoassistant.controller;

import com.example.legoassistant.repository.LegoSetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LegoSetRepository legoSetRepository;

    public HomeController(LegoSetRepository legoSetRepository) {
        this.legoSetRepository = legoSetRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("legoSets", legoSetRepository.findAll());
        return "index";
    }
}
