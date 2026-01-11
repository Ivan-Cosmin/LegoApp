package com.example.legoassistant.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AskRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void rejectsBlankQuestion() {
        AskRequest req = new AskRequest();
        req.setQuestion(" ");
        Set<ConstraintViolation<AskRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsInvalidToneRole() {
        AskRequest req = new AskRequest();
        req.setQuestion("Hello");
        req.setTone("angry");
        req.setRole("pirate");

        Set<ConstraintViolation<AskRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void acceptsValidToneRole() {
        AskRequest req = new AskRequest();
        req.setQuestion("Hello");
        req.setTone("friendly");
        req.setRole("teacher");

        Set<ConstraintViolation<AskRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }
}

