# flowable-saas-poc

最小 PoC：基于 Flowable 的多租户流程中台（共享 DB + tenantId 标注），支持 MySQL、通过 HTTP 调用流程中台接口进行流程流转。

主要特性（PoC）
- Spring Boot + Flowable
- MySQL（Docker Compose）
- 简单租户 onboarding：POST /tenants?tenantId=tenant_abc&name=TenantA，会为租户部署示例流程并返回 apiKey
- 非侵入：业务系统通过 HTTP（X-Tenant-Id header）调用中台 API 启动流程、查询任务、完成任务

运行步骤
1. 启动 MySQL：
   docker compose up -d
2. 等待 MySQL 启动（或查看 docker logs mysql）
3. 在项目根目录运行：
   mvn spring-boot:run

示例调用
- 创建租户并自动部署流程（返回 apiKey）
  curl -X POST "http://localhost:8080/tenants?tenantId=tenant_abc&name=TenantA"

- 启动流程（需带 X-Tenant-Id header）
  curl -X POST "http://localhost:8080/api/process/start/sampleApproval" -H "Content-Type: application/json" -H "X-Tenant-Id: tenant_abc" -d '{}'

- 查询任务
  curl "http://localhost:8080/api/tasks?assignee=" -H "X-Tenant-Id: tenant_abc"

- 完成任务（把 taskId 填入）
  curl -X POST "http://localhost:8080/api/tasks/{taskId}/complete" -H "Content-Type: application/json" -H "X-Tenant-Id: tenant_abc" -d '{}'

扩展建议
- 生产中请用更安全的 API Key / OAuth2 实现、加入监控、审计、配额与限流
- 如需更强隔离可采用多 schema / 多库策略（动态 DataSource）

