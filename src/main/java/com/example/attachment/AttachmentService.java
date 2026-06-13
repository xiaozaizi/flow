package com.example.attachment;

import com.example.storage.ObsStorageService;
import com.example.tenant.TenantContext;
import com.example.virus.ClamAVService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ObsStorageService obsStorageService;
    private final ClamAVService clamAVService;

    public AttachmentService(AttachmentRepository attachmentRepository, ObsStorageService obsStorageService, ClamAVService clamAVService) {
        this.attachmentRepository = attachmentRepository;
        this.obsStorageService = obsStorageService;
        this.clamAVService = clamAVService;
    }

    public Attachment store(String taskId, String processInstanceId, MultipartFile file, Long approvalRecordId, String uploadedBy) throws Exception {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new IllegalArgumentException("Missing X-Tenant-Id header");

        // scan file before upload
        Path tmp = Files.createTempFile("upload-", "-tmp");
        try {
            Files.copy(file.getInputStream(), tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            boolean clean = clamAVService.scan(tmp.toFile());
            if (!clean) {
                throw new IllegalArgumentException("File failed virus scan");
            }

            String objectName = obsStorageService.upload(tenantId, file);
            Attachment att = new Attachment(tenantId, taskId, processInstanceId, file.getOriginalFilename(), objectName, approvalRecordId, uploadedBy);
            return attachmentRepository.save(att);
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }

    public List<Attachment> listByTask(String taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }

    public List<Attachment> listByApproval(Long approvalRecordId) { return attachmentRepository.findByApprovalRecordId(approvalRecordId); }

    public Attachment find(Long id) { return attachmentRepository.findById(id).orElse(null); }

    public void updateApprovalId(Long id, Long approvalId) {
        attachmentRepository.setApprovalRecordId(id, approvalId);
    }
}
