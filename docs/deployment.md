# Deployment Guide

## Prerequisites
- JDK 11+
- MySQL 8.0 (database `flowable`)
- Redis 6.x (recommended for distributed locks)
- Docker & docker-compose (optional)
- ClamAV for virus scanning (optional but recommended)
- OBS (Huawei Cloud) bucket and credentials

## Environment variables (recommended)
- OBS_ENDPOINT, OBS_ACCESS_KEY, OBS_SECRET_KEY, OBS_BUCKET
- JWT_SECRET
- DB connection: SPRING_DATASOURCE_URL/USERNAME/PASSWORD or configure application.yml

## Quickstart (local PoC)
1. Start MySQL:
   docker compose up -d
2. Start ClamAV (optional, example):
   docker run -d --name clamav mkodockx/docker-clamav:alpine
3. Build & run app:
   mvn -DskipTests package
   java -jar target/flowable-saas-poc-0.0.1-SNAPSHOT.jar

## Production notes
- Use a secrets manager for OBS/DB/JWT credentials.
- Use Kubernetes + Helm for deployment; run multiple replicas and configure FLOWABLE job executor accordingly.
- Set up Prometheus/Grafana scraping for /actuator/prometheus (to be added).

