# API Overview and Notes

This file (docs/api.md) provides a human-readable summary of the main public APIs and example usage.

1. Tenant onboarding
- POST /tenants?tenantId=tenant_abc&name=TenantA
  - Response: Tenant object with apiKey

2. Authentication (PoC)
- POST /auth/token
  - Request: { username, password }
  - Response: { token }
  - Use: Authorization: Bearer <token>

3. Start process
- POST /api/process/start/{processKey}
  - Headers: X-Tenant-Id, Authorization
  - Body: { businessKey, variables }

4. Tasks
- GET /api/tasks?assignee=
  - Headers: X-Tenant-Id
- GET /api/tasks/{taskId}/detail
  - Returns approvals and presigned attachment URLs

5. Approvals & Attachments
- POST /api/tasks/{taskId}/attachments (multipart)
- POST /api/tasks/{taskId}/approve?comment=...&attachmentIds=1,2

6. Returns
- POST /api/returns/tasks/{taskId}/returnTo?targetActivityId=act_1&reason=Fix
- POST /api/returns/processes/{processInstanceId}/resubmit

7. Countersign
- POST /api/countersign/create
- POST /api/countersign/{id}/vote

8. Client callback
- POST /client/callback
  - Used by platform to notify business system (idempotent delivery)

