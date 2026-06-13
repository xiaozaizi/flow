package com.example.attachment;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Attachment {
    @Id
    @GeneratedValue
    private Long id;
    private String tenantId;
    private String taskId;
    private String processInstanceId;
    private String filename;
    private String objectName; // stored in OBS
    private Long approvalRecordId; // link to approval record
    private String uploadedBy;
    private Instant createdAt;

    public Attachment() {}

    public Attachment(String tenantId, String taskId, String processInstanceId, String filename, String objectName, Long approvalRecordId, String uploadedBy) {
        this.tenantId = tenantId;
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
        this.filename = filename;
        this.objectName = objectName;
        this.approvalRecordId = approvalRecordId;
        this.uploadedBy = uploadedBy;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getTaskId() { return taskId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public String getFilename() { return filename; }
    public String getObjectName() { return objectName; }
    public Long getApprovalRecordId() { return approvalRecordId; }
    public String getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
