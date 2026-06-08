# SupervisionLivraisons

A real-time delivery supervision system composed of an Android mobile app and a Node.js/Express REST + WebSocket backend, backed by PostgreSQL.

## Architecture

```
SupervisionLivraisons/
├── android/          # Native Android app (Java, MVVM)
├── backend/          # Node.js / Express API + Socket.IO
│   ├── src/
│   │   ├── controllers/   # authController, deliveryController, driverController,
│   │   │                  # dashboardController, messageController, positionController
│   │   ├── routes/
│   │   ├── services/      # socketService (real-time events)
│   │   ├── middleware/    # JWT auth
│   │   ├── config/        # database connection
│   │   └── app.js
│   └── db/init.sql
├── postman/           # Postman collections, environments, specs
└── docker-compose.yml
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Android app | Java, MVVM, Retrofit 2, Room, Socket.IO client, MPAndroidChart |
| Backend | Node.js, Express 4, Socket.IO 4 |
| Auth | JWT (jsonwebtoken + bcryptjs) |
| Database | PostgreSQL 16 |
| Containerisation | Docker Compose |

## Features

### Controller app (supervision dashboard)
- Live driver position map and tracking view
- Delivery list with status filters and detail view
- Real-time messaging to drivers
- Dashboard with MPAndroidChart statistics

### Driver app
- Home screen with today's deliveries
- Delivery detail and status update
- Delivery history
- Driver profile
- Send urgency / incident report

### Backend API
- JWT-based authentication (register / login)
- REST endpoints for deliveries, drivers, dashboard, messages, positions
- Real-time position and message broadcast via Socket.IO
- Room (local cache) on Android for offline resilience

## Prerequisites

- Docker & Docker Compose
- Android Studio (Hedgehog or later) with JDK 17
- Android device / emulator API 24+

## Quick Start

### 1. Start the backend and database

```bash
docker compose up -d
```

The API will be available at `http://localhost:3000`.

### 2. Run the Android app

Open `android/` in Android Studio, wait for Gradle sync, then run on a device or emulator.

> The app targets `http://10.0.2.2:3000/` by default (Android emulator localhost alias).  
> Change `API_BASE_URL` in `android/app/build.gradle` for a real device or remote server.

## Environment Variables (backend)

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `3000` | HTTP port |
| `PGHOST` | `db` | PostgreSQL host |
| `PGPORT` | `5432` | PostgreSQL port |
| `PGDATABASE` | `BDG_LivraisonCom_25` | Database name |
| `PGUSER` | `postgres` | Database user |
| `PGPASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | `change_me_in_production` | JWT signing secret |
| `JWT_EXPIRES_IN` | `12h` | Token lifetime |

## API Testing

Postman collections, environments, mock specs, and contract specs are in the `postman/` directory.

## Development (without Docker)

```bash
cd backend
npm install
# create a .env with the variables above pointing to a local Postgres instance
npm run dev
```
