package com.example.audit;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Audit {
    @Id
    @GeneratedValue
    private Long id;
    private String tenantId;
    private String userId;
    private String action; // e.g., START_PROCESS, COMPLETE_TASK, ASSIGN, DELEGATE, RETURN, WITHDRAW, CC, COUNTERSIGN
    @Lob
    private String detail;
    private Instant createdAt;

    public Audit() {}

    public Audit(String tenantId, String userId, String action, String detail) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.action = action;
        this.detail = detail;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
