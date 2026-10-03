# Running Pitlane Locally

## Prerequisites

- Java 23 or newer
- Node.js and npm
- RabbitMQ is optional for local development

## Start the Backend

From the repository root in PowerShell:

```powershell
$env:APP_BOOTSTRAP_ENABLED = "true"
$env:APP_BOOTSTRAP_ROOT_PASSWORD = "SystemAdmin-Pitlane-Dev-2026!"
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080`. Hibernate creates or updates the schema on startup. The default H2 database is in-memory, so its records are removed when the process exits. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to use a persistent database.

The root bootstrap runs only when enabled. On a new database it creates the `root` system administrator using `APP_BOOTSTRAP_ROOT_PASSWORD`; an existing root account is promoted to `SYSTEM_ADMIN` without changing its password.

## Start the Frontend

In a second PowerShell terminal:

```powershell
Set-Location frontend
npm install
$env:VITE_API_BASE_URL = "http://localhost:8080/api"
npm run dev
```

Open `http://localhost:5173` and sign in. The API allows this local frontend origin by default.

## Demo Accounts

The only account automatically bootstrapped is the system administrator. Garage accounts are not seeded globally because a `SYSTEM_ADMIN` must create a garage and invite its first `GARAGE_ADMIN`.

| Role | Username | Password | Provisioning |
|---|---|---|---|
| `SYSTEM_ADMIN` | `root` | `SystemAdmin-Pitlane-Dev-2026!` | Created by the backend bootstrap command above |
| `GARAGE_ADMIN` | `garageadmin` | `GarageAdminDemo-2026!` | Example values to enter when accepting the first-admin invitation |
| `MANAGER` | `manager` | `GarageManagerDemo-2026!` | Example values to enter when accepting an invitation |
| `STAFF` | `staff` | `GarageStaffDemo-2026!` | Example values to enter when accepting an invitation |
| `USER` | `user` | `GarageUserDemo-2026!` | Example values to enter when accepting an invitation |

The four garage-role rows are examples, not pre-created accounts. Use different usernames and passwords if those names already exist in the database. These credentials are for local demos only; replace them before using a persistent or shared environment.

## Create a Garage and Invite Users

1. Sign in as `root`.
2. On the **Garage network** page, create a garage and enter the first administrator's email.
3. In local mode, RabbitMQ is disabled by default. The invitation is created and its acceptance link is shown in the response/UI; open that link and enter the demo garage-admin username and password.
4. The garage admin can invite `MANAGER`, `STAFF`, or `USER` accounts from **Team access**. Managers can invite `STAFF` or `USER`; staff can invite `USER` only. Invited users choose their username and password when accepting.

When `RABBITMQ_ENABLED=true`, invitation notifications are published to the configured RabbitMQ exchange and queue. A notification consumer/delivery service must be running to send actual email or SMS; the local no-op publisher does not deliver messages.

## Tests and Builds

```powershell
.\mvnw.cmd test
npm run typecheck --prefix frontend
npm run build --prefix frontend
```