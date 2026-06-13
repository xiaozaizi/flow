package com.example.returning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {
    Optional<ReturnRecord> findByProcessInstanceId(String processInstanceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReturnRecord r where r.processInstanceId = ?1")
    Optional<ReturnRecord> findByProcessInstanceIdForUpdate(String processInstanceId);
}
