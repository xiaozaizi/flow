package com.example.approval;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class ApprovalRecord {
    @Id
    @GeneratedValue
    private Long id;
    private String tenantId;
    private String taskId;
    private String processInstanceId;
    private String approverId;
    @Lob
    private String comment;
    private Instant createdAt;

    public ApprovalRecord() {}

    public ApprovalRecord(String tenantId, String taskId, String processInstanceId, String approverId, String comment) {
        this.tenantId = tenantId;
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
        this.approverId = approverId;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getTaskId() { return taskId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public String getApproverId() { return approverId; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
