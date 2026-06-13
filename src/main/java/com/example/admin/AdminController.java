package com.example.admin;

import com.example.audit.AuditService;
import com.example.tenant.TenantRepository;
import com.example.tenant.Tenant;
import com.example.tenant.TenantContext;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricTaskInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final TenantRepository tenantRepository;
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final AuditService auditService;

    public AdminController(TenantRepository tenantRepository, RepositoryService repositoryService, RuntimeService runtimeService, TaskService taskService, AuditService auditService) {
        this.tenantRepository = tenantRepository;
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.auditService = auditService;
    }

    // Simple admin guard: require X-Admin header = true
    private void checkAdmin(String adminHeader) {
        if (adminHeader == null || !adminHeader.equalsIgnoreCase("true")) throw new SecurityException("admin required");
    }

    @GetMapping("/tenants")
    public List<Tenant> listTenants(@RequestHeader(value = "X-Admin", required = false) String admin) {
        checkAdmin(admin);
        return tenantRepository.findAll();
    }

    @GetMapping("/process-definitions")
    public List<Map<String, Object>> listProcessDefinitions(@RequestHeader(value = "X-Admin", required = false) String admin) {
        checkAdmin(admin);
        return repositoryService.createProcessDefinitionQuery().list().stream().map(pd -> Map.of(
                "id", pd.getId(),
                "key", pd.getKey(),
                "name", pd.getName(),
                "tenantId", pd.getTenantId()
        )).collect(Collectors.toList());
    }

    @GetMapping("/process-instances")
    public List<Map<String, Object>> listProcessInstances(@RequestHeader(value = "X-Admin", required = false) String admin) {
        checkAdmin(admin);
        List<HistoricProcessInstance> list = runtimeService.createHistoricProcessInstanceQuery().list();
        return list.stream().map(h -> Map.of(
                "id", h.getId(),
                "processDefinitionId", h.getProcessDefinitionId(),
                "startTime", h.getStartTime(),
                "endTime", h.getEndTime(),
                "tenantId", h.getTenantId()
        )).collect(Collectors.toList());
    }

    @GetMapping("/tasks/todo")
    public List<Map<String, Object>> listAllTodo(@RequestHeader(value = "X-Admin", required = false) String admin) {
        checkAdmin(admin);
        List<Task> list = taskService.createTaskQuery().list();
        return list.stream().map(t -> Map.of(
                "id", t.getId(),
                "name", t.getName(),
                "assignee", t.getAssignee(),
                "processInstanceId", t.getProcessInstanceId(),
                "tenantId", t.getTenantId()
        )).collect(Collectors.toList());
    }

    @GetMapping("/tasks/done")
    public List<Map<String, Object>> listAllDone(@RequestHeader(value = "X-Admin", required = false) String admin) {
        checkAdmin(admin);
        List<HistoricTaskInstance> list = runtimeService.createHistoricTaskInstanceQuery().finished().list();
        return list.stream().map(t -> Map.of(
                "id", t.getId(),
                "name", t.getName(),
                "assignee", t.getAssignee(),
                "processInstanceId", t.getProcessInstanceId(),
                "tenantId", t.getTenantId(),
                "startTime", t.getStartTime(),
                "endTime", t.getEndTime()
        )).collect(Collectors.toList());
    }

    @GetMapping("/audits")
    public List<Map<String, Object>> listAudits(@RequestHeader(value = "X-Admin", required = false) String admin, @RequestParam(required = false) String tenantId) {
        checkAdmin(admin);
        return (tenantId == null ? auditService.findAll() : auditService.findByTenant(tenantId)).stream().map(a -> Map.of(
                "id", a.getId(),
                "tenantId", a.getTenantId(),
                "userId", a.getUserId(),
                "action", a.getAction(),
                "detail", a.getDetail(),
                "createdAt", a.getCreatedAt()
        )).collect(Collectors.toList());
    }
}
