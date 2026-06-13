package com.example.audit;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public Audit record(String tenantId, String userId, String action, String detail) {
        Audit a = new Audit(tenantId, userId, action, detail);
        return auditRepository.save(a);
    }

    public List<Audit> findByTenant(String tenantId) {
        return auditRepository.findByTenantId(tenantId);
    }

    public List<Audit> findAll() {
        return auditRepository.findAll();
    }
}
