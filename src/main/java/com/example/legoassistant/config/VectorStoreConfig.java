package com.example.legoassistant.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // This acts as our in-memory "Database" for the AI context.
        // We use the Builder pattern to satisfy the new Spring AI API.
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}