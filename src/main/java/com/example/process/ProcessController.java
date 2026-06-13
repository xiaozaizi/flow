package com.example.process;

import com.example.tenant.TenantContext;
import org.flowable.engine.RuntimeService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProcessController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public ProcessController(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @PostMapping("/process/start/{processKey}")
    public Map<String, Object> startProcess(@PathVariable String processKey, @RequestBody(required=false) Map<String, Object> variables) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new IllegalArgumentException("Missing X-Tenant-Id header");

        runtimeService.createProcessInstanceBuilder()
            .processDefinitionKey(processKey)
            .tenantId(tenantId)
            .setVariables(variables == null ? Map.of() : variables)
            .start();

        return Map.of("status", "started");
    }

    @GetMapping("/tasks")
    public List<Map<String, Object>> listTasks(@RequestParam(required=false) String assignee) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new IllegalArgumentException("Missing X-Tenant-Id header");

        var query = taskService.createTaskQuery().taskTenantId(tenantId);
        if (assignee != null && !assignee.isEmpty()) {
            query = query.taskAssignee(assignee);
        }
        List<Task> tasks = query.list();
        return tasks.stream().map(t -> Map.of(
                "id", t.getId(),
                "name", t.getName(),
                "assignee", t.getAssignee(),
                "processInstanceId", t.getProcessInstanceId()
        )).collect(Collectors.toList());
    }

    @PostMapping("/tasks/{taskId}/complete")
    public Map<String, Object> completeTask(@PathVariable String taskId, @RequestBody(required=false) Map<String, Object> variables) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new IllegalArgumentException("Missing X-Tenant-Id header");

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        if (!tenantId.equals(task.getTenantId())) throw new IllegalArgumentException("Task does not belong to tenant");

        taskService.complete(taskId, variables == null ? Map.of() : variables);
        return Map.of("status", "completed");
    }
}
