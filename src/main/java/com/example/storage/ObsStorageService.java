package com.example.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class ObsStorageService {

    private final MinioClient minioClient;

    @Value("${app.obs.bucket}")
    private String bucket;

    public ObsStorageService(@Value("${app.obs.endpoint}") String endpoint,
                             @Value("${app.obs.accessKey}") String accessKey,
                             @Value("${app.obs.secretKey}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String upload(String tenantId, MultipartFile file) throws Exception {
        String original = file.getOriginalFilename();
        String objectName = tenantId + "/" + System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + (original == null ? "file" : original.replaceAll("\\s+", "_"));
        try (InputStream is = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(args);
        }
        return objectName;
    }

    public String presignedUrl(String objectName, int expirySeconds) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectName)
                    .expiry(Duration.ofSeconds(expirySeconds))
                    .build();
            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
