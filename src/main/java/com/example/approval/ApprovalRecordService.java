package com.example.approval;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalRecordService {
    private final ApprovalRecordRepository repository;

    public ApprovalRecordService(ApprovalRecordRepository repository) {
        this.repository = repository;
    }

    public ApprovalRecord create(String tenantId, String taskId, String processInstanceId, String approverId, String comment) {
        ApprovalRecord ar = new ApprovalRecord(tenantId, taskId, processInstanceId, approverId, comment);
        return repository.save(ar);
    }

    public List<ApprovalRecord> listByTask(String taskId) {
        return repository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
}
