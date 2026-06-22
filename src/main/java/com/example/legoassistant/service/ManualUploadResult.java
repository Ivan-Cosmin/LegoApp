package com.example.legoassistant.service;

import org.springframework.core.io.Resource;

public record ManualUploadResult(
        String fileName,
        String contentType,
        String storageKey,
        long size,
        Resource resource
) {
}
