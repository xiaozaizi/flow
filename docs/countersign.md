# Countersign (会签) Notes

Updates:
- CountersignTask now includes expectedVotes and completed flag.
- Service evaluate() improved to handle ABSTAIN and PERCENT rules with >= semantics.
- Added API endpoints:
  - POST /api/countersign/create?taskId=...&processInstanceId=...&rule=...&percent=0.6&expectedVotes=3
  - POST /api/countersign/{id}/vote?voter=alice&vote=APPROVE
  - GET  /api/countersign/{id}/stats
  - POST /api/countersign/{id}/timeout  (fills missing votes as ABSTAIN and re-evaluates)

Behavior summary:
- ALL: requires no rejects; if expectedVotes provided, completes when expectedVotes reached; otherwise completes when at least one approval and no rejects (PoC behavior)
- ANY: first approval completes
- MAJORITY: approvals > rejects completes
- PERCENT: approvals / expectedVotes >= percent if expectedVotes provided; otherwise approvals / totalVotes >= percent
- ABSTAIN votes are supported and counted
- Timeout endpoint can be used by scheduler to mark missing voters as ABSTAIN
