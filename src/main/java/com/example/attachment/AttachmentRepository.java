package com.example.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTaskId(String taskId);
    List<Attachment> findByApprovalRecordId(Long approvalRecordId);

    @Modifying
    @Transactional
    @Query("update Attachment a set a.approvalRecordId = :approvalId where a.id = :id")
    void setApprovalRecordId(Long id, Long approvalId);
}
