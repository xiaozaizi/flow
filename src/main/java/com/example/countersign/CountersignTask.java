package com.example.countersign;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class CountersignTask {
    @Id @GeneratedValue
    private Long id;
    private String taskId;
    private String processInstanceId;
    private String rule; // ALL, ANY, MAJORITY, PERCENT
    private Double percent; // if rule == PERCENT
    private Instant createdAt;

    public CountersignTask() {}
    public CountersignTask(String taskId, String processInstanceId, String rule, Double percent) {
        this.taskId = taskId; this.processInstanceId = processInstanceId; this.rule = rule; this.percent = percent; this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public String getRule() { return rule; }
    public Double getPercent() { return percent; }
}
