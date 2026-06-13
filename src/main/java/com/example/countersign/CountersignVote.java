package com.example.countersign;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class CountersignVote {
    @Id @GeneratedValue
    private Long id;
    private Long countersignTaskId;
    private String voter;
    private String vote; // APPROVE / REJECT / ABSTAIN
    private Instant createdAt;

    public CountersignVote() {}
    public CountersignVote(Long countersignTaskId, String voter, String vote) { this.countersignTaskId = countersignTaskId; this.voter = voter; this.vote = vote; this.createdAt = Instant.now(); }
    public Long getId() { return id; }
    public Long getCountersignTaskId() { return countersignTaskId; }
    public String getVoter() { return voter; }
    public String getVote() { return vote; }
    public Instant getCreatedAt() { return createdAt; }
}
