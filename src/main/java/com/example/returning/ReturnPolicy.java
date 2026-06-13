package com.example.returning;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ReturnPolicy {
    @Id @GeneratedValue
    private Long id;
    private String tenantId; // tenant scope
    private String processDefinitionKey; // process scope
    private boolean allowAll; // if true, any historic target allowed
    private String allowedTargets; // comma separated activity ids

    public ReturnPolicy() {}
    public ReturnPolicy(String tenantId, String processDefinitionKey, boolean allowAll, String allowedTargets) {
        this.tenantId = tenantId;
        this.processDefinitionKey = processDefinitionKey;
        this.allowAll = allowAll;
        this.allowedTargets = allowedTargets;
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public boolean isAllowAll() { return allowAll; }
    public String getAllowedTargets() { return allowedTargets; }
    public boolean isAllowedTarget(String activityId) {
        if (allowAll) return true;
        if (allowedTargets == null || allowedTargets.isBlank()) return false;
        String[] arr = allowedTargets.split(",");
        for (String a : arr) if (a.trim().equals(activityId)) return true;
        return false;
    }
}
