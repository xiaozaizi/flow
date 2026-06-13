# Security and OBS secrets

- OBS credentials are now expected to be provided via environment variables for security. The application will read these env vars if not set in application.yml:
  - OBS_ENDPOINT (e.g., https://obs.cn-north-4.myhuaweicloud.com)
  - OBS_ACCESS_KEY
  - OBS_SECRET_KEY
  - OBS_BUCKET
  - OBS_PRESIGNED_EXPIRY (seconds)

- JWT secret for authentication should be provided via environment variable JWT_SECRET.

CI/CD example (GitHub Actions) secrets to set:
- OBS_ENDPOINT
- OBS_ACCESS_KEY
- OBS_SECRET_KEY
- OBS_BUCKET
- JWT_SECRET

In workflow, pass them as environment variables to the build/run steps.
