package com.example.tenant;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final OnboardingService onboardingService;
    private final TenantRepository tenantRepository;

    public TenantController(OnboardingService onboardingService, TenantRepository tenantRepository) {
        this.onboardingService = onboardingService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    public Tenant createTenant(@RequestParam String tenantId, @RequestParam String name) {
        return onboardingService.createTenant(tenantId, name);
    }

    @GetMapping("/{tenantId}")
    public Tenant getTenant(@PathVariable String tenantId) {
        return tenantRepository.findByTenantId(tenantId).orElseThrow(() -> new RuntimeException("Not found"));
    }
}
