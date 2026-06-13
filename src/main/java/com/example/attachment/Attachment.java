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
    private String path;
    private String uploadedBy;
    private Instant createdAt;

    public Attachment() {}

    public Attachment(String tenantId, String taskId, String processInstanceId, String filename, String path, String uploadedBy) {
        this.tenantId = tenantId;
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
        this.filename = filename;
        this.path = path;
        this.uploadedBy = uploadedBy;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getTaskId() { return taskId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public String getFilename() { return filename; }
    public String getPath() { return path; }
    public String getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
