package com.example.legoassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Validated request for asking the AI assistant.
 */
public class AskRequest {

    @NotBlank
    @Size(min = 2, max = 500)
    private String question;

    @Pattern(regexp = "friendly|concise|detailed", message = "Invalid tone")
    private String tone;

    @Pattern(regexp = "assistant|teacher|inspector", message = "Invalid role")
    private String role;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

