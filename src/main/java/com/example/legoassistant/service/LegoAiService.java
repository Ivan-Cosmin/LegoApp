package com.example.legoassistant.service;

import com.example.legoassistant.model.LegoSet;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LegoAiService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public LegoAiService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    // 1. Ingest Data (Call this only once per set!)
    public void loadManualIntoMemory(LegoSet legoSet) {
        System.out.println("📝 Indexing manual: " + legoSet.getName());

        List<Document> documents = legoSet.getSteps().stream()
                .map(step -> new Document(
                        "Step " + step.getStepNumber() + ": " + step.getInstructionText(),
                        // METADATA IS CRITICAL HERE:
                        Map.of("lego_set_id", legoSet.getId())
                ))
                .toList();

        vectorStore.add(documents);
    }

    // 2. Ask Question with Context Filtering
    public String askAssistant(Long legoSetId, String userQuery) {

        // A. Search Vector Store ONLY for this specific Lego Set
        // The filter syntax depends on the underlying store, but for Simple/Chroma:
        // "lego_set_id == 123"
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .topK(3) // Get top 3 relevant steps
                        .filterExpression("lego_set_id == " + legoSetId) // <--- THE MAGIC FILTER
                        .build()
        );

        // B. Extract text
        String context = similarDocuments.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n"));

        // If no context found, fallback generically
        if (context.isEmpty()) {
            return "I couldn't find any specific instructions for this step in the manual. Double check the step number!";
        }

        // C. Send to AI
        String prompt = """
                You are a helper for a LEGO set. 
                Strictly base your answer on the following manual instructions.
                
                Context from Manual:
                %s
                
                User Question: %s
                """.formatted(context, userQuery);

        return chatClient.prompt(prompt).call().content();
    }
}