新增：审批意见支持附件（上传、列表、下载）

接口说明：
- POST /api/tasks/{taskId}/attachments  (multipart form)
  - file: 上传的文件
  - comment: 可选的审批意见文本
  - header: X-Tenant-Id, X-User-Id
  - 返回: attachmentId, filename

- GET /api/tasks/{taskId}/attachments
  - 列出任务相关的附件元数据

- GET /api/tasks/attachments/{id}/download
  - 下载附件（需 X-Tenant-Id header 与附件的 tenant 匹配）

实现说明：
- 文件存储到服务器本地目录 uploads/{tenantId}/，并记录 Attachment 实体（taskId, processInstanceId, filename, path, uploadedBy, createdAt）
- 上传时会在 Flowable task 上添加 comment 以便在流程历史中查看
- 所有上传/下载操作都会写入审计表（Audit）

注意：当前存储采用本地文件系统（PoC）。生产建议使用对象存储（S3/MinIO）并做防病毒扫描、鉴权和访问控制。
