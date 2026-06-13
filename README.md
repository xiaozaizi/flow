# OBS & ApprovalRecord integration

- OBS (Huawei Cloud) is accessed via S3-compatible MinIO client. Configure in application.yml under app.obs.
- ApprovalRecord entity groups comment + attachments. Task detail endpoint returns approval records with presigned download URLs.
- Frontend: minimal static page under frontend/ that fetches /api/tasks/{taskId}/detail and shows approvals and attachments.

Configuration (application.yml):
- app.obs.endpoint: OBS endpoint (e.g. https://obs.cn-north-4.myhuaweicloud.com)
- app.obs.accessKey / secretKey
- app.obs.bucket

Presigned URLs expire in 1 hour by default in PoC.
