# Running Pitlane Locally

## Prerequisites

- Java 23 or newer
- Node.js and npm
- Docker Desktop is optional; it is needed only to run RabbitMQ and the Mailpit test inbox (see below)

## Start the Backend

From the repository root in PowerShell:

```powershell
$env:APP_BOOTSTRAP_ENABLED = "true"
$env:APP_BOOTSTRAP_ROOT_PASSWORD = "SystemAdmin-Pitlane-Dev-2026!"
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080`. Hibernate creates or updates the schema on startup. The default H2 database is stored in `./data/garage.mv.db`, so records survive restarts. Delete the `data/` folder to start fresh. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to use a different database.

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
3. The invitee receives an email with a one-time link (valid for 72 hours) to the **welcome page**, which shows the garage, the role, and who sent the invitation.
4. On the welcome page the invitee enters their full name, an optional phone number, a username, and a password, then selects **Continue to dashboard**. They are signed in to their garage, and the person who invited them receives an email saying the invitation was accepted.
5. The garage admin can invite `MANAGER`, `STAFF`, or `USER` accounts from **Team access**. Managers can invite `STAFF` or `USER`; staff can invite `USER` only. Inviting the same email again cancels the earlier link and sends a new one.

## How Invitation Emails Are Delivered

```
invite ──► NotificationService ──(after the database commit)──► EventPublisher
                                                                   │
            RABBITMQ_ENABLED=true:  garage.events exchange ──► garage.notifications queue ──► NotificationConsumer
            RABBITMQ_ENABLED=false: delivered directly, in the same process                      │
                                                                                                 ▼
                                                        NotificationDeliveryService ──► SMTP (MAIL_ENABLED=true)
                                                                                    └─► application log (otherwise)
```

With RabbitMQ enabled, a delivery that keeps failing is retried three times and then moved to the `garage.notifications.dlq` queue, where you can inspect it in the RabbitMQ management UI.

### Quick mode: no RabbitMQ, no email server

This is the default. Each email, including its invitation link, is printed in the backend log under `Email delivery is disabled`. Copy the link from the log into the browser.

### Full mode: RabbitMQ and a test inbox

`docker-compose.yml` starts RabbitMQ and [Mailpit](https://mailpit.axllent.org/), a local inbox that catches every email the backend sends. Docker is used only for these two services; the application still runs with `mvnw` and `npm`.

```powershell
docker compose up -d
$env:APP_BOOTSTRAP_ENABLED = "true"
$env:APP_BOOTSTRAP_ROOT_PASSWORD = "SystemAdmin-Pitlane-Dev-2026!"
$env:RABBITMQ_ENABLED = "true"
$env:MAIL_ENABLED = "true"
.\mvnw.cmd spring-boot:run
```

- Invitation emails: http://localhost:8025
- RabbitMQ management (`guest` / `guest`): http://localhost:15672
- Stop the services with `docker compose down`.

To send real email instead, set `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH=true`, `MAIL_SMTP_STARTTLS=true`, and `MAIL_FROM` for your SMTP provider.

Set `INVITATION_EXPOSE_LINK=true` to also return the link in the API response and show an **Open invite** button in the admin UI. Use this for development only.

## Tests and Builds

```powershell
.\mvnw.cmd test
npm run typecheck --prefix frontend
npm run build --prefix frontend
```