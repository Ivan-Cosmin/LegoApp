package com.example.legoassistant.service;

import com.example.legoassistant.model.ChatHistory;
import com.example.legoassistant.model.LegoSet;
import com.example.legoassistant.model.User;
import com.example.legoassistant.repository.ChatHistoryRepository;
import com.example.legoassistant.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LegoAiService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;

    public LegoAiService(VectorStore vectorStore,
                         ChatClient.Builder chatClientBuilder,
                         ChatHistoryRepository chatHistoryRepository,
                         UserRepository userRepository) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.chatHistoryRepository = chatHistoryRepository;
        this.userRepository = userRepository;
    }

    // ======================================================
    // 1. INGESTION: For Uploaded Files (.txt, .pdf)
    // ======================================================
    public void processAndIndexFile(LegoSet legoSet, Resource fileResource) {
        System.out.println("⏳ Incepem parsarea manualului pentru setul: " + legoSet.getName());

        // 1. Read the file (Works for TXT and PDF)
        TikaDocumentReader reader = new TikaDocumentReader(fileResource);
        List<Document> rawDocuments = reader.get();

        // 2. Split text into chunks
        TokenTextSplitter splitter = new TokenTextSplitter(200, 40, 5, 1000, true);
        List<Document> splitDocuments = splitter.apply(rawDocuments);

        // 3. Tag every chunk with the Lego Set ID (Metadata)
        for (Document doc : splitDocuments) {
            doc.getMetadata().put("lego_set_id", legoSet.getId());
        }

        // 4. Save to VectorStore
        vectorStore.add(splitDocuments);

        System.out.println("✅ Am incarcat si vectorizat " + splitDocuments.size() + " bucati de text in PostgreSQL.");
    }

    // ======================================================
    // 2. SEARCH & ANSWER (RAG)
    // ======================================================
    public String askAssistant(Long legoSetId, String userQuery) {
        return askAssistant(legoSetId, userQuery, null, null);
    }

    public String askAssistant(Long legoSetId, String userQuery, String tone, String role) {
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .topK(3)
                        .filterExpression("lego_set_id == " + legoSetId)
                        .build()
        );

        String manualContext = similarDocuments.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        String username = currentUsername();
        String conversationContext = buildConversationContext(legoSetId, username);

        if (manualContext.isBlank()) {
            String fallback = "I checked the manual but couldn't find specific instructions for that. " +
                    "Try asking about the Set Theme or Part Numbers if you uploaded a text file.";
            saveChatHistory(legoSetId, userQuery, fallback);
            return fallback;
        }

        String styleInstruction = buildStyleInstruction(tone, role);

        String prompt = """
                You are an expert LEGO Building Assistant.
                %s
                Answer the user's question strictly using the manual context below.
                If the context lists steps, guide the user clearly.

                CONVERSATION SO FAR:
                %s

                MANUAL CONTEXT:
                %s

                USER QUESTION: %s
                """.formatted(styleInstruction, conversationContext, manualContext, userQuery);

        String answer = chatClient.prompt(prompt).call().content();
        saveChatHistory(legoSetId, userQuery, answer);
        return answer;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String name = auth.getName();
        return (name == null || "anonymousUser".equals(name)) ? null : name;
    }

    private String buildConversationContext(Long legoSetId, String username) {
        if (legoSetId == null || username == null) {
            return "(no prior messages)";
        }

        List<ChatHistory> recent = chatHistoryRepository
                .findTop10ByLegoSet_IdAndUser_UsernameOrderByTimestampDesc(legoSetId, username);

        if (recent == null || recent.isEmpty()) {
            return "(no prior messages)";
        }

        List<ChatHistory> chronological = new ArrayList<>(recent);
        Collections.reverse(chronological);

        return chronological.stream()
                .map(m -> "User: " + safe(m.getUserMessage()) + "\nAssistant: " + safe(m.getAiResponse()))
                .collect(Collectors.joining("\n\n"));
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private String buildStyleInstruction(String tone, String role) {
        String safeTone = (tone == null || tone.isBlank()) ? "friendly" : tone.trim().toLowerCase();
        String safeRole = (role == null || role.isBlank()) ? "assistant" : role.trim().toLowerCase();

        String toneLine = switch (safeTone) {
            case "concise" -> "Use a concise tone.";
            case "detailed" -> "Use a detailed, explanatory tone.";
            default -> "Use a friendly tone.";
        };

        String roleLine = switch (safeRole) {
            case "teacher" -> "Act like a step-by-step teacher: ask clarifying questions when needed and give numbered steps.";
            case "inspector" -> "Act like a mistake checker: point out likely errors and suggest what to verify.";
            default -> "Act like a helpful general assistant.";
        };

        return toneLine + " " + roleLine;
    }

    // ======================================================
    // 3. CHAT HISTORY
    // ======================================================
    private void saveChatHistory(Long legoSetId, String userQuery, String aiResponse) {
        try {
            ChatHistory history = new ChatHistory();
            history.setUserMessage(userQuery);
            history.setAiResponse(aiResponse);
            history.setTimestamp(LocalDateTime.now());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
                Optional<User> user = userRepository.findByUsername(auth.getName());
                user.ifPresent(history::setUser);
            }

            if (legoSetId != null) {
                LegoSet stub = new LegoSet();
                stub.setId(legoSetId);
                history.setLegoSet(stub);
            }

            chatHistoryRepository.save(history);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to persist chat history: " + e.getMessage());
        }
    }
}