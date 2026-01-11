package com.example.legoassistant.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LegoSetValidationTest {

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
    void legoSetRejectsBlankNameAndTheme() {
        LegoSet set = new LegoSet();
        set.setName(" ");
        set.setTheme("");
        set.setSetNumber(1);

        assertThat(validator.validate(set)).isNotEmpty();
    }

    @Test
    void legoSetRejectsNonPositiveSetNumber() {
        LegoSet set = new LegoSet();
        set.setName("X");
        set.setTheme("Y");
        set.setSetNumber(0);

        assertThat(validator.validate(set)).isNotEmpty();
    }
}

