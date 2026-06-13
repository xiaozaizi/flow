package com.example.returning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReturnPolicyRepository extends JpaRepository<ReturnPolicy, Long> {
    Optional<ReturnPolicy> findByTenantIdAndProcessDefinitionKey(String tenantId, String processDefinitionKey);
}
