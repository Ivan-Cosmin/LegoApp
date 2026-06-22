package com.example.legoassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsStorageConfig {

    @Bean
    public S3Client s3Client(AwsStorageProperties storageProperties) {
        return S3Client.builder()
                .region(Region.of(storageProperties.getRegion()))
                .build();
    }
}
