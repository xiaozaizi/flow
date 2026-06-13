package com.example.returning;

import javax.persistence.*;

@Entity
public class ReturnRecord {
    @Id @GeneratedValue
    private Long id;
    private String processInstanceId;
    private Integer times;
    private String lastReturnBy;
    private String lastReason;
    private java.time.Instant updatedAt;

    public ReturnRecord() {}
    public ReturnRecord(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        this.times = 0;
        this.updatedAt = java.time.Instant.now();
    }

    public Long getId() { return id; }
    public String getProcessInstanceId() { return processInstanceId; }
    public Integer getTimes() { return times; }
    public void increment() { this.times = (this.times==null?1:this.times+1); this.updatedAt = java.time.Instant.now(); }
    public void setLast(String by, String reason) { this.lastReturnBy = by; this.lastReason = reason; this.updatedAt = java.time.Instant.now(); }
}
