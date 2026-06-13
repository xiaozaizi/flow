package com.example.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTaskId(String taskId);
    List<Attachment> findByApprovalRecordId(Long approvalRecordId);
}
