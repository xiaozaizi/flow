package com.example.tenant;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OnboardingService {

    private final TenantRepository tenantRepository;
    private final RepositoryService repositoryService;

    public OnboardingService(TenantRepository tenantRepository, RepositoryService repositoryService) {
        this.tenantRepository = tenantRepository;
        this.repositoryService = repositoryService;
    }

    public Tenant createTenant(String tenantId, String name) {
        // create simple apiKey for PoC
        String apiKey = UUID.randomUUID().toString();
        Tenant t = new Tenant();
        t.setTenantId(tenantId);
        t.setName(name);
        t.setApiKey(apiKey);
        tenantRepository.save(t);

        // Deploy sample process for this tenant
        Deployment deployment = repositoryService.createDeployment()
            .name("default-deployment-for-" + tenantId)
            .addClasspathResource("processes/sample-approve.bpmn20.xml")
            .tenantId(tenantId)
            .deploy();

        return t;
    }
}
