package com.example.legoassistant.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {

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
    void userRejectsInvalidEmail() {
        User u = new User();
        u.setUsername("admin");
        u.setPassword("x");
        u.setEmail("not-an-email");

        assertThat(validator.validate(u)).isNotEmpty();
    }

    @Test
    void userRejectsBlankUsernamePasswordEmail() {
        User u = new User();
        u.setUsername(" ");
        u.setPassword(" ");
        u.setEmail(" ");

        assertThat(validator.validate(u)).isNotEmpty();
    }
}

