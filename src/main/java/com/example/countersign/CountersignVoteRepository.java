package com.example.countersign;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CountersignVoteRepository extends JpaRepository<CountersignVote, Long> {
    List<CountersignVote> findByCountersignTaskId(Long countersignTaskId);
}
