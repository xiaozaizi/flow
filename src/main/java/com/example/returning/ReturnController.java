package com.example.returning;

import org.springframework.web.bind.annotation.*;
import com.example.tenant.TenantContext;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping("/tasks/{taskId}/returnTo")
    public ResponseEntity<?> returnTo(@PathVariable String taskId,
                                      @RequestParam String targetActivityId,
                                      @RequestParam String reason,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body("Missing X-Tenant-Id");
        returnService.returnTo(taskId, targetActivityId, reason, userId);
        return ResponseEntity.ok().body("returned");
    }

    @PostMapping("/processes/{processInstanceId}/resubmit")
    public ResponseEntity<?> resubmit(@PathVariable String processInstanceId, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        returnService.resubmit(processInstanceId, userId);
        return ResponseEntity.ok().body("resubmitted");
    }
}
