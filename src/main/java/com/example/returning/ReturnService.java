package com.example.returning;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.audit.AuditService;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReturnService {

    private final ReturnRecordRepository repo;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final ReturnPolicyRepository policyRepository;
    private final AuditService auditService;
    private final RestTemplate restTemplate;

    @Value("${app.return.max-times:3}")
    private int maxTimes;

    public ReturnService(ReturnRecordRepository repo, RuntimeService runtimeService, TaskService taskService, RepositoryService repositoryService, ReturnPolicyRepository policyRepository, AuditService auditService, RestTemplate restTemplate) {
        this.repo = repo;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.policyRepository = policyRepository;
        this.auditService = auditService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public void returnTo(String taskId, String targetActivityId, String reason, String userId) {
        var task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        String piId = task.getProcessInstanceId();

        // determine tenant/process definition key
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(piId).singleResult();
        String processDefinitionId = pi.getProcessDefinitionId();
        ProcessDefinition pd = repositoryService.getProcessDefinition(processDefinitionId);
        String processDefinitionKey = pd.getKey();
        String tenantId = task.getTenantId();

        // check policy: admin must define allowed targetActivityIds for this tenant/process
        Optional<ReturnPolicy> maybePolicy = policyRepository.findByTenantIdAndProcessDefinitionKey(tenantId, processDefinitionKey);
        if (maybePolicy.isEmpty()) {
            throw new IllegalStateException("Return policy not configured for tenant/process. Contact admin.");
        }
        ReturnPolicy policy = maybePolicy.get();
        if (!policy.isAllowAll()) {
            if (targetActivityId == null || targetActivityId.isBlank()) {
                throw new IllegalArgumentException("targetActivityId required");
            }
            if (!policy.isAllowedTarget(targetActivityId)) {
                throw new IllegalArgumentException("Target activity not allowed by admin policy");
            }
        }

        var rrOpt = repo.findByProcessInstanceIdForUpdate(piId);
        ReturnRecord rr = rrOpt.orElseGet(() -> new ReturnRecord(piId));
        if (rr.getTimes() != null && rr.getTimes() >= maxTimes) throw new IllegalStateException("Return limit reached");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Return reason required");

        // mark process variable status
        runtimeService.setVariable(piId, "status", "IN_MODIFICATION");

        // create new execution before target
        runtimeService.createProcessInstanceModification(piId).startBeforeActivity(targetActivityId).execute();

        // complete current task to move execution
        taskService.complete(taskId);

        rr.increment();
        rr.setLast(userId, reason);
        repo.save(rr);

        // audit
        auditService.record(tenantId, userId, "RETURN", "taskId=" + taskId + ",returnTo=" + targetActivityId + ",reason=" + reason + ",times=" + rr.getTimes());

        // immediate callback to business system if configured
        sendCallbackIfConfigured(piId, "RETURNED", rr.getId(), tenantId);
    }

    public void resubmit(String processInstanceId, String userId) {
        // resubmit means set status back to active and continue
        runtimeService.setVariable(processInstanceId, "status", "ACTIVE");
        // notify business system about resubmit if configured
        auditService.record(null, userId, "RESUBMIT", "processInstanceId=" + processInstanceId);
        sendCallbackIfConfigured(processInstanceId, "RESUBMITTED", null, null);
    }

    private void sendCallbackIfConfigured(String processInstanceId, String status, Long returnRecordId, String tenantId) {
        try {
            Object cbObj = runtimeService.getVariable(processInstanceId, "businessCallbackUrl");
            if (cbObj == null) cbObj = runtimeService.getVariable(processInstanceId, "businessCallback");
            if (cbObj == null) return;
            String callbackUrl = cbObj.toString();

            Object businessKeyObj = runtimeService.getVariable(processInstanceId, "businessKey");
            String businessKey = businessKeyObj == null ? null : businessKeyObj.toString();

            String deliveryId = UUID.randomUUID().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Delivery-Id", deliveryId);

            Map<String, Object> payload = Map.of(
                    "processInstanceId", processInstanceId,
                    "businessKey", businessKey,
                    "status", status,
                    "returnRecordId", returnRecordId
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(callbackUrl, entity, String.class);
            // record callback audit
            auditService.record(tenantId, "system", "CALLBACK_SENT", "url=" + callbackUrl + ",deliveryId=" + deliveryId + ",status=" + status);
        } catch (Exception e) {
            // log and record failed callback
            auditService.record(tenantId, "system", "CALLBACK_FAILED", "processInstanceId=" + processInstanceId + ",err=" + e.getMessage());
        }
    }
}
