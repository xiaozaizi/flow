package com.example.attachment;

import com.example.storage.ObsStorageService;
import com.example.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ObsStorageService obsStorageService;

    public AttachmentService(AttachmentRepository attachmentRepository, ObsStorageService obsStorageService) {
        this.attachmentRepository = attachmentRepository;
        this.obsStorageService = obsStorageService;
    }

    public Attachment store(String taskId, String processInstanceId, MultipartFile file, Long approvalRecordId, String uploadedBy) throws Exception {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new IllegalArgumentException("Missing X-Tenant-Id header");

        String objectName = obsStorageService.upload(tenantId, file);
        Attachment att = new Attachment(tenantId, taskId, processInstanceId, file.getOriginalFilename(), objectName, approvalRecordId, uploadedBy);
        return attachmentRepository.save(att);
    }

    public List<Attachment> listByTask(String taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }

    public List<Attachment> listByApproval(Long approvalRecordId) { return attachmentRepository.findByApprovalRecordId(approvalRecordId); }

    public Attachment find(Long id) { return attachmentRepository.findById(id).orElse(null); }
}
