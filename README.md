# 🚗 CampusRide

> A full-stack student carpooling platform connecting university students traveling between cities through ride offers, bookings, real-time communication, and trusted community features.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![React](https://img.shields.io/badge/React-TypeScript-61DAFB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

---

## 📖 Overview

CampusRide is a peer-to-peer carpooling platform built for university students traveling between cities.
It enables students to publish ride offers, request available seats, communicate in real time, and build trust through a mutual rating system.

---

## 💡 Why CampusRide?

Ride-sharing services such as Uber are not available in most parts of Serbia and the surrounding region. As a result, students typically organize intercity rides through Facebook groups or messaging apps.

CampusRide provides a dedicated platform where students can publish ride offers, request available seats, communicate in real time, and build trust through verified profiles and a mutual rating system.

---

## 📸 Preview

> Screenshots will be added as the UI is implemented.

---

## ✨ Features

- Publish ride offers between cities
- Search available rides by destination and departure date
- Request seats with driver approval workflow
- Chat with drivers and passengers in real time
- Receive instant booking and ride notifications
- Track upcoming and completed rides from a personal dashboard
- Rate fellow travelers to build a trusted student community
- View ride routes on an interactive map

---

## 🏗️ Architecture

The application follows a **Modular Monolith** architecture.

```
┌─────────────────────────────────┐
│         Frontend (React)        │
└────────────────┬────────────────┘
                 │
        REST API + WebSocket
                 │
┌────────────────▼────────────────┐
│           Spring Boot           │
│  ┌───────┐  ┌────────────────┐  │
│  │ Auth  │  │     Users      │  │
│  ├───────┤  ├────────────────┤  │
│  │ Rides │  │    Bookings    │  │
│  ├───────┤  ├────────────────┤  │
│  │ Chat  │  │ Notifications  │  │
│  └───────┘  ├────────────────┤  │
│             │    Ratings     │  │
│             └────────────────┘  │
│                                 │
└───┬────────┬──────────┬─────────┘
    │        │          │
┌───▼──┐ ┌───▼───┐ ┌────▼─────┐
│  PG  │ │ Redis │ │ RabbitMQ │
└──────┘ └───────┘ └──────────┘
```

The backend follows a **Modular Monolith** architecture where each domain module owns its controllers, services, repositories, and entities.

Asynchronous communication between modules is handled through **RabbitMQ** domain events, while **STOMP WebSockets** provide real-time chat and notifications.

**Redis** is used for online presence detection, rate limiting, and temporary location caching.

---

## 🔧 Technical Highlights

- Atomic seat booking to prevent double reservations
- Event-driven communication using RabbitMQ domain events
- Real-time ride chat with STOMP WebSockets
- Personal notification channel for each authenticated user
- Redis-based online presence and temporary caching
- Secure JWT authentication with role-based authorization
- Dockerized local development environment

---

## 🛠️ Tech Stack

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- RabbitMQ
- Redis
- STOMP WebSockets
- Swagger/OpenAPI

### Frontend

- React
- TypeScript
- Tailwind CSS
- React Router
- Axios
- Leaflet
- SockJS
- STOMP.js

### DevOps

- Docker
- Docker Compose
- GitHub Actions
- Pre-commit Hooks
- Spotless
- ESLint
- Prettier

---

## 🚀 Getting Started

### Clone

```bash
git clone https://github.com/dmitar-strbac/campusride.git
cd campusride
```

### Start infrastructure

```bash
docker compose up -d
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

After starting the application:

| Service             | URL                                   |
| ------------------- | ------------------------------------- |
| Frontend            | http://localhost:5173                 |
| Backend API         | http://localhost:8080                 |
| Swagger UI          | http://localhost:8080/swagger-ui.html |
| RabbitMQ Management | http://localhost:15672                |

---

## 📁 Project Structure

```
campusride
│
├── backend
├── frontend
├── docker-compose.yml
└── README.md
```

---

## 🧪 Quality & Tooling

The project follows automated code quality checks before every commit.

- ESLint for frontend static analysis
- Prettier for frontend code formatting
- Spotless (Google Java Format) for backend formatting
- Git pre-commit hooks for automated quality checks
- Docker Compose for local development
- GitHub Actions CI for build and test automation

---

## 📌 Roadmap

- [x] Project setup
- [ ] Authentication
- [ ] Ride management
- [ ] Booking workflow
- [ ] Chat
- [ ] Notifications
- [ ] Ratings
- [ ] Maps
- [ ] CI/CD
- [ ] Deployment

---

## 📄 License

This project is licensed under the **MIT License**.
See the `LICENSE` file for details.

---

## 👤 Author

**Dmitar Štrbac**
📧 Contact: [dmitarstrbac04@gmail.com](mailto:dmitarstrbac04@gmail.com)
🗓️ 2026
