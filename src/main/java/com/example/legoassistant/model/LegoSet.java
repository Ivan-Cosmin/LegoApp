package com.example.legoassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
public class LegoSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Set name cannot be empty")
    private String name;

    @NotBlank(message = "Theme is required")
    private String theme;

    @Min(value = 1, message = "Set number must be positive")
    private Integer setNumber;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "legoSet", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BuildingStep> steps = new ArrayList<>();

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public Integer getSetNumber() { return setNumber; }
    public void setSetNumber(Integer setNumber) { this.setNumber = setNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<BuildingStep> getSteps() { return steps; }
    public void setSteps(List<BuildingStep> steps) { this.steps = steps; }
}