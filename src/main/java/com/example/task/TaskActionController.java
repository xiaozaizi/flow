package com.example.task;

import com.example.audit.AuditService;
import com.example.tenant.TenantContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskActionController {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final AuditService auditService;

    public TaskActionController(TaskService taskService, RuntimeService runtimeService, AuditService auditService) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.auditService = auditService;
    }

    @PostMapping("/{taskId}/approve")
    public Map<String, Object> approve(@PathVariable String taskId, @RequestBody(required = false) Map<String, Object> variables, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        if (!task.getTenantId().equals(tenantId)) throw new IllegalArgumentException("Task not in tenant");

        taskService.complete(taskId, variables == null ? Map.of() : variables);
        auditService.record(tenantId, userId, "COMPLETE_TASK", "taskId=" + taskId + ",vars=" + (variables==null?"{}":variables.toString()));
        return Map.of("status", "completed");
    }

    @PostMapping("/{taskId}/assign")
    public Map<String, Object> assign(@PathVariable String taskId, @RequestParam String assignee, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        taskService.setAssignee(taskId, assignee);
        auditService.record(tenantId, userId, "ASSIGN", "taskId=" + taskId + ",assignee=" + assignee);
        return Map.of("status", "assigned");
    }

    @PostMapping("/{taskId}/delegate")
    public Map<String, Object> delegate(@PathVariable String taskId, @RequestParam String delegateTo, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        taskService.delegateTask(taskId, delegateTo);
        auditService.record(tenantId, userId, "DELEGATE", "taskId=" + taskId + ",to=" + delegateTo);
        return Map.of("status", "delegated");
    }

    @PostMapping("/{taskId}/cc")
    public Map<String, Object> cc(@PathVariable String taskId, @RequestParam String user, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        taskService.addUserIdentityLink(taskId, user, "cc");
        taskService.addComment(taskId, task.getProcessInstanceId(), "CC to " + user);
        auditService.record(tenantId, userId, "CC", "taskId=" + taskId + ",ccUser=" + user);
        return Map.of("status", "cced");
    }

    @PostMapping("/{taskId}/return")
    public Map<String, Object> returnToPrevious(@PathVariable String taskId, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String tenantId = TenantContext.getTenantId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        String piId = task.getProcessInstanceId();

        // find last completed user task in history
        var historyService = org.flowable.engine.impl.util.CommandContextUtil.getProcessEngineConfiguration().getHistoryService();
        var histTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(piId)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();
        if (histTasks.isEmpty()) throw new IllegalArgumentException("No previous task to return to");
        String targetActivityId = histTasks.get(0).getTaskDefinitionKey();

        // create activity before target
        runtimeService.createProcessInstanceModification(piId).startBeforeActivity(targetActivityId).execute();

        // complete current task
        taskService.complete(taskId);
        auditService.record(tenantId, userId, "RETURN", "taskId=" + taskId + ",returnTo=" + targetActivityId);
        return Map.of("status", "returned", "returnTo", targetActivityId);
    }

    @PostMapping("/{taskId}/withdraw")
    public Map<String, Object> withdraw(@PathVariable String taskId, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        String piId = task.getProcessInstanceId();

        // check initiator
        var runtime = runtimeService;
        var vars = runtime.getVariables(piId);
        Object initiator = vars.get("initiator");
        if (initiator == null || !initiator.equals(userId)) {
            throw new IllegalArgumentException("Only initiator can withdraw");
        }
        runtimeService.deleteProcessInstance(piId, "withdraw by " + userId);
        auditService.record(TenantContext.getTenantId(), userId, "WITHDRAW", "processInstanceId=" + piId);
        return Map.of("status", "withdrawn");
    }

    @PostMapping("/{taskId}/countersign/add")
    public Map<String, Object> addCountersign(@PathVariable String taskId, @RequestParam String user, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // For PoC: treat countersign as adding a candidate user to the task (parallel countersign needs model support)
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        taskService.addCandidateUser(taskId, user);
        auditService.record(TenantContext.getTenantId(), userId, "COUNTERSIGN_ADD", "taskId=" + taskId + ",user=" + user);
        return Map.of("status", "countersign_added");
    }
}
