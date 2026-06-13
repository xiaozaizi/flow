# Architecture and Design

This document describes the high-level architecture of the Flowable-based SaaS Workflow Mid-platform PoC and planned production features.

## Overview
- Backend: Spring Boot + Flowable engine (embedded) + MySQL
- Storage: Huawei OBS (S3-compatible) for attachments
- Auth: JWT for API access (PoC); plan for OAuth2 in production
- Admin UI: Vue 3 + Element Plus (separate frontend project, built static into backend at /frontend)
- Client example: standalone Spring Boot client showing how to start processes and receive callbacks

## Modules
- core (Spring Boot app)
  - tenant management
  - onboarding (deploy default processes)
  - process APIs (start, tasks, complete, assign, delegate)
  - approval records + attachments
  - storage (OBS wrapper)
  - audit
  - countersign (vote management)
  - returns (return to historical node / resubmit)
- admin-ui (Vue 3 + Element Plus)
- client-sample (Spring Boot client)

## Multi-tenancy
- Flowable tenantId used to isolate process definitions and instances.
- Tenant metadata stored in Tenant entity.
- Per-tenant configuration planned (timeouts, notifications, policies).

## High level data model
- Tenant
- ApprovalRecord (taskId, processInstanceId, approverId, comment, createdAt)
- Attachment (objectName in OBS, approvalRecordId)
- Audit (logs)
- ReturnRecord (processInstanceId, times, lastReturnBy, lastReason)
- CountersignVote (countersignTaskId, voter, vote, time)

## Scalability & HA (summary)
- Run Flowable job executors as multiple instances with DB/Redis locks
- Use MinIO/OBS for attachments (no local FS)
- Archive historic data to ES or partitioned MySQL
- Use message queue (Kafka/RabbitMQ) for notifications and async tasks

