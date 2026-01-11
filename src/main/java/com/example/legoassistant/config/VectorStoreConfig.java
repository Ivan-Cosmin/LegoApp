package com.example.legoassistant.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 1. Define the file path
        File storeFile = new File("src/main/resources/vector_store.json");

        // 2. Load existing vectors if the file exists
        if (storeFile.exists()) {
            System.out.println("📂 Loading AI Memory from file: " + storeFile.getAbsolutePath());
            vectorStore.load(storeFile);
        } else {
            System.out.println("📂 No existing memory found. Starting fresh.");
        }

        return vectorStore;
    }
}