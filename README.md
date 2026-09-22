# FamilyFlow

FamilyFlow is a family task and virtual-banking application. Parents can create a family group, assign rewarded tasks and manage virtual accounts; children can join the group, track work and learn basic money habits.

The repository contains the current web application and the earlier Java Swing implementation that it evolved from.

## Features

- Parent and child registration, sign-in and family-group membership
- Task assignment, urgency, deadlines, recurring tasks and completion approval
- Reward and bonus payouts linked to virtual accounts
- Checking and fixed-deposit accounts with deposit, withdrawal and transfer flows
- Responsive React dashboard backed by a Spring Boot REST API
- Local JSON persistence for an easy-to-run demonstration

## Repository layout

```text
backend/         Spring Boot 4 REST API and integration tests
frontend/        React 19 + Vite client
desktop-legacy/  Original Java Swing application and tests
```

The current application follows this dependency direction:

```text
React client → REST controllers → domain services → JSON store
```

## Requirements

- JDK 17 or newer
- Maven 3.9 or newer
- Node.js 20 or newer
- pnpm 10 or newer

## Run the web application

Start the API:

```bash
cd backend
mvn spring-boot:run
```

In a second terminal, start the client:

```bash
cd frontend
pnpm install
pnpm dev
```

Open `http://localhost:5173`. The Vite development server proxies `/api` and `/actuator` requests to `http://localhost:8080`.

Useful environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | API port |
| `APP_DATA_DIR` | `../data` | JSON data directory |
| `LEGACY_DATA_DIR` | `..` | Optional legacy-data import directory |
| `CORS_ORIGIN` | `http://localhost:5173` | Allowed browser origin |

## Test and build

```bash
cd backend
mvn test

cd ../frontend
pnpm test
pnpm build
```

The legacy desktop client can be tested separately:

```bash
cd desktop-legacy
mvn test
```

## Data and security

Runtime files such as `users.json`, `accounts.json`, `tasks.json` and `transactions.json` may contain local user data. They are excluded from version control together with logs, dependency directories and build output.

This is a course and portfolio project, not a real banking system. Its local JSON storage and in-memory session model are intended for demonstration only and should not be used for production credentials or financial data.
