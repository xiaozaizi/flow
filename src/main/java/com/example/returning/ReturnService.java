package com.example.returning;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReturnService {

    private final ReturnRecordRepository repo;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Value("${app.return.max-times:3}")
    private int maxTimes;

    public ReturnService(ReturnRecordRepository repo, RuntimeService runtimeService, TaskService taskService) {
        this.repo = repo;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @Transactional
    public void returnTo(String taskId, String targetActivityId, String reason, String userId) {
        var task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found");
        String piId = task.getProcessInstanceId();

        var rr = repo.findByProcessInstanceId(piId).orElseGet(() -> new ReturnRecord(piId));
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
