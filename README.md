# Nurun v1

A backend-driven AI orchestration system with persistent conversation memory.

## Core Features (v1)
- **JWT Authentication:** Secure user access.
- **Conversation Persistence:** History stored in PostgreSQL.
- **Provider Abstraction:** Initial integration with Google Gemini API.
- **Memory Reconstruction:** Context is rebuilt from DB for every request.

## Tech Stack
- **Backend:** Spring Boot, Java
- **Database:** PostgreSQL
- **Deployment:** Docker, DigitalOcean Droplet
- **Version Control:** Absolute Git / Conventional Commits

## Git Strategy
- `main`: Production-ready.
- `develop`: Integration branch.
- `feature/*`: Development branches.