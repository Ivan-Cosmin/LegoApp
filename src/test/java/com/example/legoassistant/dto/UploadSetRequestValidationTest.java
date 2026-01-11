package com.example.legoassistant.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UploadSetRequestValidationTest {

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
    void invalidWhenBlankFields() {
        UploadSetRequest req = new UploadSetRequest();
        req.setName(" ");
        req.setTheme("");
        req.setSetNumber(null);
        req.setFile(null);

        Set<ConstraintViolation<UploadSetRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void validWhenAllFieldsPresent() {
        UploadSetRequest req = new UploadSetRequest();
        req.setName("Death Star");
        req.setTheme("Star Wars");
        req.setSetNumber(10188);
        req.setFile(new MockMultipartFile("file", "manual.txt", "text/plain", "hello".getBytes()));

        Set<ConstraintViolation<UploadSetRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }
}
