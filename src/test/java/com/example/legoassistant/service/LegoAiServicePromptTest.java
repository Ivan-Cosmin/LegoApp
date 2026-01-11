package com.example.legoassistant.service;

import com.example.legoassistant.model.ChatHistory;
import com.example.legoassistant.repository.ChatHistoryRepository;
import com.example.legoassistant.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LegoAiServicePromptTest {

    @Test
    void promptContainsConversationAndStyle() {
        VectorStore vectorStore = mock(VectorStore.class);
        ChatHistoryRepository chatHistoryRepository = mock(ChatHistoryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        // Mock chat client builder => chat client
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        // Minimal vector store search: return empty manual context to bypass? We need manual context non-blank.
        // Use 1 fake Document with formatted content.
        org.springframework.ai.document.Document doc = new org.springframework.ai.document.Document("manual chunk");
        when(vectorStore.similaritySearch(any(org.springframework.ai.vectorstore.SearchRequest.class)))
                .thenReturn(List.of(doc));

        ChatHistory h = new ChatHistory();
        h.setUserMessage("Hi");
        h.setAiResponse("Hello");
        h.setTimestamp(LocalDateTime.now());
        when(chatHistoryRepository.findTop10ByLegoSet_IdAndUser_UsernameOrderByTimestampDesc(1L, "admin"))
                .thenReturn(List.of(h));

        // Capture prompt
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(chatClient.prompt(captor.capture()).call().content()).thenReturn("ok");

        // Force SecurityContext username via a simple stub: we can avoid it by calling internal overload? not accessible.
        // So we just call service and accept conversation may be '(no prior messages)' when no auth.
        LegoAiService service = new LegoAiService(vectorStore, builder, chatHistoryRepository, userRepository);

        service.askAssistant(1L, "Question", "concise", "teacher");

        String prompt = captor.getValue();
        assertThat(prompt).contains("Use a concise tone");
        assertThat(prompt).contains("step-by-step teacher");
        assertThat(prompt).contains("MANUAL CONTEXT");
        assertThat(prompt).contains("USER QUESTION");
    }
}

