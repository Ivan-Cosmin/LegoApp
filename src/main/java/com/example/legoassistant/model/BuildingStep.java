package com.example.legoassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class BuildingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer stepNumber;

    @NotBlank(message = "Instructions cannot be empty")
    @Column(length = 1000)
    private String instructionText;

    @ManyToOne
    @JoinColumn(name = "lego_set_id")
    private LegoSet legoSet;

    // --- CONSTRUCTORS ---
    public BuildingStep() {}

    // --- GETTERS AND SETTERS (Manually added to fix error) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(String instructionText) {
        this.instructionText = instructionText;
    }

    public LegoSet getLegoSet() {
        return legoSet;
    }

    public void setLegoSet(LegoSet legoSet) {
        this.legoSet = legoSet;
    }
}