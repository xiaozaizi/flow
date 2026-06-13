package com.example.task;

import com.example.approval.ApprovalRecord;
import com.example.approval.ApprovalRecordService;
import com.example.attachment.Attachment;
import com.example.attachment.AttachmentService;
import com.example.audit.AuditService;
import com.example.storage.ObsStorageService;
import com.example.tenant.TenantContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskActionController {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final AuditService auditService;
    private final AttachmentService attachmentService;
    private final ApprovalRecordService approvalRecordService;
    private final ObsStorageService obsStorageService;

    public TaskActionController(TaskService taskService, RuntimeService runtimeService, AuditService auditService, AttachmentService attachmentService, ApprovalRecordService approvalRecordService, ObsStorageService obsStorageService) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.auditService = auditService;
        this.attachmentService = attachmentService;
        this.approvalRecordService = approvalRecordService;
        this.obsStorageService = obsStorageService;
    }

    // Start/complete endpoints remain similar (snipped for brevity) - keep approve
    @PostMapping("/{taskId}/approve")
    public Map<String, Object> approve(@PathVariable String taskId, @RequestParam(value = "comment", required = false) String comment,
                                       @RequestParam(value = "attachmentIds", required = false) List<Long> attachmentIds,
                                       @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        if (!task.getTenantId().equals(tenantId)) throw new IllegalArgumentException("Task not in tenant");

        // create approval record
        ApprovalRecord ar = approvalRecordService.create(tenantId, taskId, task.getProcessInstanceId(), userId, comment == null ? "" : comment);
        // associate provided attachments
        if (attachmentIds != null) {
            for (Long aid : attachmentIds) {
                Attachment att = attachmentService.find(aid);
                if (att != null && Objects.equals(att.getTaskId(), taskId)) {
                    // naive update: since Attachment fields are final in entity, skip DB update here in PoC.
                    // In production, add setter or update query to set approvalRecordId.
                }
            }
        }

        taskService.complete(taskId);
        auditService.record(tenantId, userId, "COMPLETE_TASK", "taskId=" + taskId + ",approvalId=" + ar.getId());
        return Map.of("status", "completed", "approvalId", ar.getId());
    }

    // upload attachment and create approval record when comment present
    @PostMapping(path = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadAttachment(@PathVariable String taskId,
                                                @RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "comment", required = false) String comment,
                                                @RequestHeader(value = "X-User-Id", required = false) String userId) throws Exception {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        if (!task.getTenantId().equals(tenantId)) throw new IllegalArgumentException("Task not in tenant");

        // if comment provided, create approval record first
        Long approvalId = null;
        if (comment != null && !comment.isEmpty()) {
            ApprovalRecord ar = approvalRecordService.create(tenantId, taskId, task.getProcessInstanceId(), userId, comment);
            approvalId = ar.getId();
        }

        Attachment att = attachmentService.store(taskId, task.getProcessInstanceId(), file, approvalId, userId);
        // add comment to flowable task for traceability
        taskService.addComment(taskId, task.getProcessInstanceId(), "Attachment: " + att.getFilename() + (comment != null ? ", comment: " + comment : ""));
        auditService.record(tenantId, userId, "ATTACHMENT_ADD", "taskId=" + taskId + ",attachmentId=" + att.getId());
        return Map.of("status", "uploaded", "attachmentId", att.getId(), "filename", att.getFilename(), "approvalId", approvalId);
    }

    @GetMapping("/{taskId}/attachments")
    public List<Map<String, Object>> listAttachments(@PathVariable String taskId) {
        List<Attachment> list = attachmentService.listByTask(taskId);
        return list.stream().map(a -> Map.of(
                "id", a.getId(),
                "filename", a.getFilename(),
                "uploadedBy", a.getUploadedBy(),
                "createdAt", a.getCreatedAt()
        )).collect(Collectors.toList());
    }

    @GetMapping("/attachments/{id}/download")
    public Map<String, Object> presignedDownload(@PathVariable Long id) {
        Attachment a = attachmentService.find(id);
        if (a == null) throw new IllegalArgumentException("Not found");
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || !tenantId.equals(a.getTenantId())) throw new IllegalArgumentException("Access denied");

        String url = obsStorageService.presignedUrl(a.getObjectName(), 60*60);
        auditService.record(tenantId, null, "ATTACHMENT_DOWNLOAD", "attachmentId=" + id);
        return Map.of("url", url);
    }

    // task detail endpoint: task info + approval records + attachments with presigned urls
    @GetMapping("/{taskId}/detail")
    public Map<String, Object> taskDetail(@PathVariable String taskId) {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        if (!task.getTenantId().equals(tenantId)) throw new IllegalArgumentException("Task not in tenant");

        // basic task info
        Map<String, Object> info = new HashMap<>();
        info.put("id", task.getId());
        info.put("name", task.getName());
        info.put("assignee", task.getAssignee());
        info.put("processInstanceId", task.getProcessInstanceId());

        // approval records
        List<ApprovalRecord> approvals = approvalRecordService.listByTask(taskId);
        List<Map<String, Object>> approvalViews = new ArrayList<>();
        for (ApprovalRecord ar : approvals) {
            List<Attachment> atts = attachmentService.listByApproval(ar.getId());
            List<Map<String, Object>> attViews = atts.stream().map(a -> Map.of(
                    "id", a.getId(),
                    "filename", a.getFilename(),
                    "uploadedBy", a.getUploadedBy(),
                    "createdAt", a.getCreatedAt(),
                    "downloadUrl", obsStorageService.presignedUrl(a.getObjectName(), 60*60)
            )).collect(Collectors.toList());
            approvalViews.add(Map.of(
                    "id", ar.getId(),
                    "approverId", ar.getApproverId(),
                    "comment", ar.getComment(),
                    "createdAt", ar.getCreatedAt(),
                    "attachments", attViews
            ));
        }

        info.put("approvals", approvalViews);
        return Map.of("task", info);
    }
}
