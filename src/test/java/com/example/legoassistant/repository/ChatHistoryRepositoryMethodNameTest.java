package com.example.legoassistant.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight test to ensure repository method names exist and compile.
 */
class ChatHistoryRepositoryMethodNameTest {

    @Test
    void methodNamesExist() {
        assertThat(ChatHistoryRepository.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("findTop10ByLegoSet_IdAndUser_UsernameOrderByTimestampDesc"));
    }
}
