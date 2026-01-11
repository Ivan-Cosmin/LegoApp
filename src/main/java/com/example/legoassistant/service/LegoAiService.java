package com.example.legoassistant.service;

import com.example.legoassistant.model.LegoSet;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LegoAiService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public LegoAiService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    // ======================================================
    // 1. INGESTION: For Uploaded Files (.txt, .pdf)
    // ======================================================
    public void processAndIndexFile(LegoSet legoSet, Resource fileResource) {
        System.out.println("📄 Parsing manual for set: " + legoSet.getName());

        // 1. Read the file (Works for TXT and PDF)
        TikaDocumentReader reader = new TikaDocumentReader(fileResource);
        List<Document> rawDocuments = reader.get();

        // 2. Split text into chunks (Context Window management)
        // 400 tokens per chunk, overlapping 50 tokens to keep context across boundaries
        TokenTextSplitter splitter = new TokenTextSplitter(400, 300, 5, 1000, true);
        List<Document> splitDocuments = splitter.apply(rawDocuments);

        // 3. Tag every chunk with the Lego Set ID (Metadata)
        for (Document doc : splitDocuments) {
            doc.getMetadata().put("lego_set_id", legoSet.getId());
        }

        // 4. Save to Memory & Disk
        vectorStore.add(splitDocuments);
        persistVectorStore();

        System.out.println("✅ Indexed " + splitDocuments.size() + " chunks for " + legoSet.getName());
    }

    // ======================================================
    // 2. SEARCH & ANSWER (RAG)
    // ======================================================
    public String askAssistant(Long legoSetId, String userQuery) {
        // 1. Search Vector Store ONLY for this specific Lego Set
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .topK(3) // Get top 3 most relevant matches
                        .filterExpression("lego_set_id == " + legoSetId) // STRICT FILTER
                        .build()
        );

        // 2. Extract content
        String context = similarDocuments.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        // 3. Handle case with no info
        if (context.isEmpty()) {
            return "I checked the manual but couldn't find specific instructions for that. " +
                    "Try asking about the Set Theme or Part Numbers if you uploaded a text file.";
        }

        // 4. Construct Prompt
        String prompt = """
                You are an expert LEGO Building Assistant.
                Answer the user's question strictly using the manual context below.
                If the context lists steps, guide the user clearly.
                
                MANUAL CONTEXT:
                %s
                
                USER QUESTION: %s
                """.formatted(context, userQuery);

        // 5. Call AI
        return chatClient.prompt(prompt).call().content();
    }

    // ======================================================
    // 3. HELPER: Save Memory to File
    // ======================================================
    private void persistVectorStore() {
        // We cast to SimpleVectorStore to access the save method
        if (vectorStore instanceof SimpleVectorStore simpleStore) {
            String VECTOR_STORE_PATH = "src/main/resources/vector_store.json";
            File storeFile = new File(VECTOR_STORE_PATH);
            simpleStore.save(storeFile);
            System.out.println("💾 AI Memory saved to: " + storeFile.getAbsolutePath());
        }
    }
}