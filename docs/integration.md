### Callback behavior

When a flow is returned (API: POST /api/returns/tasks/{taskId}/returnTo), the platform will immediately attempt to call the business system callback URL if the process instance contains the variable `businessCallbackUrl` (or `businessCallback`).

Payload (JSON):
{
  "processInstanceId": "...",
  "businessKey": "...",
  "status": "RETURNED|RESUBMITTED",
  "returnRecordId": 123
}

Headers:
- X-Delivery-Id: unique-id-for-idempotency

Delivery semantics:
- The platform will attempt a synchronous POST when the return action occurs. Failures are recorded in the audit log (CALLBACK_FAILED) and can be retried by admins if needed. In production we recommend using an async message queue + retry policy with exponential backoff and dead-letter handling.
