# AI Expense Tracker

AI Expense Tracker is a full-stack personal finance application with a React frontend and a Spring Boot backend.  
It helps users manage expenses, income, budgets, and categories, and includes AI-driven insights and chat support.

## Tech Stack

- **Frontend:** React 19, Vite, Material UI, Axios, Recharts
- **Backend:** Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL
- **AI Integration:** Ollama (local model support)

## Repository Structure

```text
AI_EXPENSE_TRACKER/
├── Expense_calculator_UI/   # React frontend
└── expense-tracker-api/     # Spring Boot backend
```

## Core Features

- User authentication (register, login, password reset with OTP)
- Expense tracking (create, update, delete, filter)
- Income management
- Budget and category management
- Dashboard with summary and visual analytics
- Notifications
- AI insights and AI chat for finance-related assistance

## Prerequisites

- Node.js (LTS recommended)
- Java 25 (as configured in `expense-tracker-api/pom.xml`)
- PostgreSQL
- Maven (or use Maven Wrapper included in backend)
- Ollama (optional, required for AI endpoints)

## Backend Setup (`expense-tracker-api`)

1. Go to backend folder:
   - `cd expense-tracker-api`
2. Create `application.properties` from the example:
   - Copy `src/main/resources/application-example.properties` to `src/main/resources/application.properties`
3. Update these values:
   - `spring.datasource.url`
   - `spring.datasource.username`
   - `spring.datasource.password`
   - `jwt.secret`
   - (optional) `ollama.base-url`, `ollama.model`
4. Run the backend:
   - Linux/macOS: `./mvnw spring-boot:run`
   - Windows: `mvnw.cmd spring-boot:run`

Backend runs on `http://localhost:8080`.

## Frontend Setup (`Expense_calculator_UI`)

1. Go to frontend folder:
   - `cd Expense_calculator_UI`
2. Install dependencies:
   - `npm install`
3. Run development server:
   - `npm run dev`

Frontend runs on `http://localhost:5173` and calls backend API at `http://localhost:8080/api`.

## Useful Commands

### Frontend

- `npm run dev` – start dev server
- `npm run build` – create production build
- `npm run lint` – run ESLint
- `npm run preview` – preview production build

### Backend

- `./mvnw spring-boot:run` – run app
- `./mvnw test` – run tests
- `./mvnw clean package` – build jar

## API Base Paths (High-level)

- `/api/auth` – authentication and password recovery
- `/api/expenses` – expense management
- `/api/income` – income management
- `/api/budget` – budgets and progress
- `/api/categories` – categories
- `/api/dashboard` – dashboard summary data
- `/api/notifications` – notifications
- `/api/ai-insights` – AI-generated insights
- `/api/ai-chat` – AI chat and financial context
- `/api/profile` – profile and password updates

## Notes

- CORS is configured for `http://localhost:5173`.
- To use AI features locally, ensure Ollama is running with the configured model.
