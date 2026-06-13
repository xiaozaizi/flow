# Integration Guide (Business Systems)

## Basic flow
1. Tenant application -> platform creates tenant and returns API credentials
2. Business system obtains token (JWT or API key)
3. Business system calls Start Process API with businessKey and optional form schema
4. Platform starts process and returns processInstanceId
5. Platform calls business system callback upon process completion or critical events (idempotent delivery)

## Sample start request
- POST /api/process/start/{processKey}
- Headers: Authorization: Bearer <token>, X-Tenant-Id
- Body: { "businessKey": "leave-123", "variables": {...} }

## Callback contract
- POST <businessCallbackUrl>
- Payload: { "processInstanceId": "...", "businessKey": "...", "status": "COMPLETED|RETURNED|WITHDRAWN", "approvalId": 123 }
- Delivery: retries with exponential backoff; platform stores delivery attempts and ensures idempotency by sending a unique deliveryId in header X-Delivery-Id

