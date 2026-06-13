package com.example.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class ObsStorageService {

    private MinioClient minioClient;

    @Value("${app.obs.endpoint:}")
    private String endpoint;

    @Value("${app.obs.accessKey:}")
    private String accessKey;

    @Value("${app.obs.secretKey:}")
    private String secretKey;

    @Value("${app.obs.bucket:}")
    private String bucket;

    @Value("${app.obs.presignedExpirySeconds:3600}")
    private int presignedExpirySeconds;

    @PostConstruct
    public void init() {
        String ep = endpoint != null && !endpoint.isEmpty() ? endpoint : System.getenv("OBS_ENDPOINT");
        String ak = accessKey != null && !accessKey.isEmpty() ? accessKey : System.getenv("OBS_ACCESS_KEY");
        String sk = secretKey != null && !secretKey.isEmpty() ? secretKey : System.getenv("OBS_SECRET_KEY");
        if (ep == null || ak == null || sk == null) {
            throw new IllegalStateException("OBS configuration not set. Please provide app.obs.endpoint and OBS_ACCESS_KEY/OBS_SECRET_KEY env vars.");
        }
        this.minioClient = MinioClient.builder().endpoint(ep).credentials(ak, sk).build();
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

    public int getPresignedExpirySeconds() { return presignedExpirySeconds; }
}
