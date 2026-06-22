package com.example.legoassistant.service;

import com.example.legoassistant.config.AwsStorageProperties;
import com.example.legoassistant.model.LegoSet;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class S3FileService {

    private final AwsStorageProperties storageProperties;
    private final S3Client s3Client;

    public S3FileService(AwsStorageProperties storageProperties, S3Client s3Client) {
        this.storageProperties = storageProperties;
        this.s3Client = s3Client;
    }

    public ManualUploadResult uploadManual(LegoSet legoSet, MultipartFile file) throws IOException {
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = extensionFrom(originalFilename);

        if (isS3Enabled()) {
            return uploadToS3(legoSet, file, originalFilename, extension);
        }

        return uploadLocally(legoSet, file, originalFilename, extension);
    }

    private boolean isS3Enabled() {
        return "s3".equalsIgnoreCase(storageProperties.getType())
                && StringUtils.hasText(storageProperties.getS3().getBucket());
    }

    private ManualUploadResult uploadToS3(LegoSet legoSet,
                                          MultipartFile file,
                                          String originalFilename,
                                          String extension) throws IOException {
        String key = "manuals/" + legoSet.getId() + "/" + UUID.randomUUID() + extension;
        String bucket = storageProperties.getS3().getBucket();

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        }

        legoSet.setManualFileName(originalFilename);
        legoSet.setManualContentType(file.getContentType());
        legoSet.setManualS3Key(key);
        legoSet.setManualSize(file.getSize());

        Resource resourceForIndexing = copyToTemporaryResource(file, extension);
        return new ManualUploadResult(originalFilename, file.getContentType(), key, file.getSize(), resourceForIndexing);
    }

    private ManualUploadResult uploadLocally(LegoSet legoSet,
                                             MultipartFile file,
                                             String originalFilename,
                                             String extension) throws IOException {
        Path localDir = Paths.get(storageProperties.getLocalDir());
        Files.createDirectories(localDir);

        String key = UUID.randomUUID() + "-" + originalFilename;
        Path target = localDir.resolve(key);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        legoSet.setManualFileName(originalFilename);
        legoSet.setManualContentType(file.getContentType());
        legoSet.setManualS3Key(null);
        legoSet.setManualSize(file.getSize());

        return new ManualUploadResult(
                originalFilename,
                file.getContentType(),
                localDir.relativize(target).toString(),
                file.getSize(),
                new FileSystemResource(target)
        );
    }

    private Resource copyToTemporaryResource(MultipartFile file, String extension) throws IOException {
        Path tempFile = Files.createTempFile("lego-manual-", extension);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        tempFile.toFile().deleteOnExit();
        return new FileSystemResource(tempFile);
    }

    private String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "manual.txt";
        }

        String base = Paths.get(filename).getFileName().toString();
        base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        return base.toLowerCase(Locale.ROOT);
    }

    private String extensionFrom(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot > 0 && dot < filename.length() - 1) {
            return filename.substring(dot);
        }
        return ".txt";
    }
}
