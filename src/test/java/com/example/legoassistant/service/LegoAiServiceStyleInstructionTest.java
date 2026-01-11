package com.example.legoassistant.service;

import com.example.legoassistant.repository.ChatHistoryRepository;
import com.example.legoassistant.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;

import static org.mockito.Mockito.*;

/**
 * Simple smoke test: calling askAssistant with invalid tone/role should not crash.
 */
class LegoAiServiceStyleInstructionTest {

    @Test
    void askAssistantDoesNotCrashOnNullToneRole() {
        VectorStore vectorStore = mock(VectorStore.class);
        ChatHistoryRepository chatHistoryRepository = mock(ChatHistoryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        LegoAiService service = new LegoAiService(vectorStore, builder, chatHistoryRepository, userRepository);
        // We don't assert output here; we just ensure no exception from prompt building when no docs (manualContext blank)
        service.askAssistant(1L, "hello");
    }
}

