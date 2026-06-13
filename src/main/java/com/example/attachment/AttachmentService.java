package com.example.attachment;

import com.example.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Attachment store(String taskId, String processInstanceId, MultipartFile file, String uploadedBy) throws IOException {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new IllegalArgumentException("Missing X-Tenant-Id header");

        // ensure tenant dir
        Path tenantPath = Path.of(uploadDir, tenantId);
        Files.createDirectories(tenantPath);

        String filename = file.getOriginalFilename();
        String storedName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + (filename == null ? "file" : filename.replaceAll("\\s+", "_"));
        Path target = tenantPath.resolve(storedName);
        try (var is = file.getInputStream()) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }

        Attachment att = new Attachment(tenantId, taskId, processInstanceId, filename, target.toString(), uploadedBy);
        return attachmentRepository.save(att);
    }

    public List<Attachment> listByTask(String taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }

    public Optional<Attachment> find(Long id) {
        return attachmentRepository.findById(id);
    }
}
