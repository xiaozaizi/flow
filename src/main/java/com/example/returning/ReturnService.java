package com.example.returning;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReturnService {

    private final ReturnRecordRepository repo;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final ReturnPolicyRepository policyRepository;

    @Value("${app.return.max-times:3}")
    private int maxTimes;

    public ReturnService(ReturnRecordRepository repo, RuntimeService runtimeService, TaskService taskService, RepositoryService repositoryService, ReturnPolicyRepository policyRepository) {
        this.repo = repo;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.policyRepository = policyRepository;
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
    }

    public void resubmit(String processInstanceId, String userId) {
        // resubmit means set status back to active and continue
        runtimeService.setVariable(processInstanceId, "status", "ACTIVE");
    }
}
