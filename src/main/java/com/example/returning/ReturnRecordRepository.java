package com.example.returning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {
    Optional<ReturnRecord> findByProcessInstanceId(String processInstanceId);
}
