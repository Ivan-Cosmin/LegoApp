package com.example.legoassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
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

    // --- GETTERS AND SETTERS PENTRU AWS ---
    private String manualFileName;

    private String manualContentType;

    private String manualS3Key;

    private Long manualSize;

    @OneToMany(mappedBy = "legoSet", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BuildingStep> steps = new ArrayList<>();

}