package com.example.legoassistant;

import com.example.legoassistant.model.BuildingStep;
import com.example.legoassistant.model.LegoSet;
import com.example.legoassistant.model.User;
import com.example.legoassistant.repository.LegoSetRepository;
import com.example.legoassistant.repository.UserRepository;
import com.example.legoassistant.service.LegoAiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LegoAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegoAssistantApplication.class, args);
    }

    @Bean
    public CommandLineRunner initialDataLoader(UserRepository userRepository,
                                               LegoSetRepository legoSetRepository,
                                               LegoAiService legoAiService) {
        return args -> {
            // 1. Create User
            if (userRepository.count() == 0) {
                User user = new User();
                user.setUsername("lego_master");
                user.setPassword("securePassword123");
                user.setEmail("master@lego.com");
                userRepository.save(user);
            }

            // 2. Create LEGO Sets (Only if DB is empty)
            if (legoSetRepository.count() == 0) {

                // --- SET 1: STAR WARS ---
                LegoSet falcon = new LegoSet();
                falcon.setName("Millennium Falcon");
                falcon.setSetNumber(75192);
                falcon.setTheme("Star Wars");
                falcon.setDescription("The fastest hunk of junk in the galaxy.");

                addStep(falcon, 1, "Assemble the Technic frame rectangle.");
                addStep(falcon, 2, "Attach the rear landing gear legs.");
                addStep(falcon, 3, "Connect the hyperdrive generator to the core.");

                legoSetRepository.save(falcon);
                // Load into AI
                legoAiService.loadManualIntoMemory(falcon);


                // --- SET 2: HARRY POTTER ---
                LegoSet hogwarts = new LegoSet();
                hogwarts.setName("Hogwarts Castle");
                hogwarts.setSetNumber(71043);
                hogwarts.setTheme("Harry Potter");
                hogwarts.setDescription("The magical school of witchcraft and wizardry.");

                addStep(hogwarts, 1, "Build the boathouse base with gray plates.");
                addStep(hogwarts, 2, "Construct the Great Hall entrance doors.");
                addStep(hogwarts, 3, "Attach the moving staircase tower.");

                legoSetRepository.save(hogwarts);
                // Load into AI
                legoAiService.loadManualIntoMemory(hogwarts);

                System.out.println("✅ Two manuals loaded and indexed!");
            }
        };
    }

    // Helper method to make code cleaner
    private void addStep(LegoSet set, int number, String text) {
        BuildingStep step = new BuildingStep();
        step.setStepNumber(number);
        step.setInstructionText(text);
        step.setLegoSet(set);
        set.getSteps().add(step);
    }
}