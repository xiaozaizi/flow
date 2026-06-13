package com.example.countersign;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CountersignTaskRepository extends JpaRepository<CountersignTask, Long> {
    CountersignTask findByTaskId(String taskId);
}
