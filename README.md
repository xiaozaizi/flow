更新：增加后台管理和任务操作。

新增接口（简要说明）

1) 管理后台（Admin，需 Header X-Admin: true）
- GET /admin/tenants                      列出所有租户
- GET /admin/process-definitions         列出流程定义
- GET /admin/process-instances           列出流程实例记录（History）
- GET /admin/tasks/todo                  列出所有待办
- GET /admin/tasks/done                  列出所有已办
- GET /admin/audits?tenantId=...         审计记录（管理员查看）

2) 任务操作（租户范围，需 X-Tenant-Id header）
- POST /api/tasks/{taskId}/approve       完成任务（审批通过）
- POST /api/tasks/{taskId}/assign?assignee=uid   转派任务
- POST /api/tasks/{taskId}/delegate?delegateTo=uid  委派任务
- POST /api/tasks/{taskId}/cc?user=uid   抄送（在任务上添加 identity link 和 comment）
- POST /api/tasks/{taskId}/return        退回到上一个已完成的用户任务（PoC 自动查找并在该活动前重新启动执行）
- POST /api/tasks/{taskId}/withdraw     撤回流程（仅发起人可撤回，删除流程实例）
- POST /api/tasks/{taskId}/countersign/add?user=uid  会签：添加会签参与人为候选人（PoC 简化）

3) 审计记录
- 所有关键操作会记录到 audit 表（Audit 实体），管理员可查看全量或按租户过滤。

说明与限制（PoC）
- Admin 权限判断仅基于 header X-Admin: true，请在生产中替换为真正鉴权机制（OAuth2/JWT + 角色）。
- 退回（RETURN）通过在目标用户任务前启动一个新的执行（runtimeService.createProcessInstanceModification.startBeforeActivity）并完成当前任务实现；这在简单流程中工作良好，但复杂流程网关/并行分支下行为需仔细验证。
- 会签（COUNTERSIGN）以添加任务候选用户实现，完整并行会签需要在 BPMN 中建模为 multi-instance userTask。
- 撤回（WITHDRAW）直接删除流程实例，仅当当前用户与 initiator 相同时允许。

已把代码提交到仓库 main 分支（你可以查看变更）。

下一步建议（你可以选）
- 将 Admin 接口加入前端简单页面（React/Vue）进行管理展示，我可以帮你生成一个最简管理 UI。 
- 把 Admin 接口鉴权替换为 OAuth2/JWT，我可以集成 Spring Security 和示例配置。 
- 为退回/会签写更多单元测试与流程模型示例，验证复杂场景下的行为。

请选择下一步（例如："做 UI" 或 "接入 OAuth2" 或 "写测试"）。
