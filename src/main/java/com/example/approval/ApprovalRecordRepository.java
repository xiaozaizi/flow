package com.example.approval;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {
    List<ApprovalRecord> findByTaskIdOrderByCreatedAtDesc(String taskId);
}
