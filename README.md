# 💰 AI Expense Tracker

An AI-powered personal finance management application built with **Spring Boot, React, PostgreSQL, and Ollama**.

AI Expense Tracker helps users manage their personal finances by tracking expenses and income, managing categories and monthly budgets, receiving financial notifications, and generating AI-powered financial insights.

---

## 📌 Overview

AI Expense Tracker is a full-stack personal finance management application designed to provide users with a centralized platform for managing and understanding their financial activity.

The application allows users to:

- Track daily expenses and income
- Organize transactions using categories
- Set monthly budgets
- Monitor spending against budgets
- Receive budget alerts
- Receive daily expense reminders
- Generate AI-powered financial insights
- Interact with an AI financial assistant
- Analyze daily and monthly financial activity
- Manage profile and notification settings securely

The application follows a **React frontend + Spring Boot REST API + PostgreSQL database** architecture with AI functionality integrated through Ollama.

---

# ✨ Features

## 🔐 Authentication & Security

- User registration
- User login
- JWT-based authentication
- Protected REST API endpoints
- BCrypt password hashing
- Change password
- Forgot password functionality
- OTP-based password reset verification
- Password reset token validation
- Token expiration handling
- OTP attempt handling
- Input validation
- User-specific data access

---

## 💸 Expense Management

Users can manage their expenses through a complete CRUD workflow.

### Supported Operations

- Add expense
- View expenses
- Update expense
- Delete expense
- Search expenses
- Filter expenses
- Filter by category
- Filter by payment method
- Filter by date
- View expense totals
- View category-wise spending

### Payment Methods

- Cash
- Credit Card
- Debit Card
- UPI
- Bank Transfer

---

## 💵 Income Management

Users can maintain their income records and track their financial inflow.

### Supported Operations

- Add income
- View income
- Update income
- Delete income
- Filter income
- Track income over time
- Compare income with expenses

---

## 🏷️ Category Management

The application supports categorization of financial transactions.

Users can:

- View categories
- Create custom categories
- Update categories
- Delete categories
- Filter transactions by category

The category system supports both predefined categories and user-created custom categories.

---

## 🎯 Budget Management

Users can create monthly budgets to control their spending.

### Budget Features

- Create monthly budgets
- Set category-specific spending limits
- View budgets
- Update budgets
- Delete budgets
- Monitor spending against budget limits
- Receive budget alerts

---

## 🔔 Notification System

The application provides notifications for important financial events.

### Notification Types

- Budget alerts
- AI insights
- Expense reminders
- System notifications

### Notification Features

- View notifications
- View unread notification count
- Mark individual notifications as read
- Mark all notifications as read
- Delete notifications
- Configure notification preferences

---

## ⏰ Expense Reminders

The application can remind users to record their daily expenses.

The reminder system:

1. Checks whether expense reminders are enabled.
2. Checks the user's configured reminder time.
3. Checks whether an expense has been recorded for the current day.
4. Creates a reminder when required.
5. Prevents duplicate reminders for the same day.

The reminder functionality is implemented using a scheduled backend service.

---

## 🚨 Budget Alerts

The application monitors configured budgets and generates notifications when spending reaches defined thresholds.

Supported alert thresholds include:

- 80%
- 90%
- 100%

Duplicate alerts for the same budget threshold and period are prevented.

---

# 🤖 AI Features

## 🧠 AI-Powered Financial Insights

The application generates AI-powered insights based on the user's financial activity.

Insights can be generated for:

- Daily activity
- Weekly activity
- Monthly activity

The AI insight system analyzes relevant financial information and generates user-oriented financial insights.

AI-generated insights can also be delivered through the application's notification system.

---

## 💬 AI Financial Assistant

The application provides an AI chat assistant that allows users to interact with their financial information conversationally.

The AI assistant can work with financial context such as:

- Expenses
- Income
- Budgets
- Categories
- Spending patterns

The application also maintains AI conversation history so users can continue their financial discussions.

---

## 🦙 Ollama Integration

AI functionality is integrated using **Ollama**, allowing the application to communicate with a locally hosted AI model.

The Ollama integration is used for:

- Financial insights
- AI-powered financial conversations
- Context-aware financial assistance

The exact Ollama model can be configured according to the development environment.

---

# 📊 Dashboard

The dashboard provides a centralized overview of the user's financial activity.

It includes information such as:

- Recent expenses
- Recent income
- Spending summaries
- Category-wise spending
- Budget information
- Savings information
- Financial trends
- AI-generated insights

---

# 👤 User Profile & Settings

Users can manage their account and application preferences through the settings section.

Supported functionality includes:

- View profile
- Update profile
- Change password
- Configure expense reminders
- Configure budget alerts
- Configure AI insight notifications
- Configure system notifications
- Configure reminder time

---

# 🏗️ System Architecture

The application follows a full-stack architecture:

```text
                    ┌──────────────────────┐
                    │      React UI        │
                    │      Frontend        │
                    └──────────┬───────────┘
                               │
                               │ Axios / REST API
                               ▼
                    ┌──────────────────────┐
                    │   Spring Boot API    │
                    │      Backend         │
                    └──────────┬───────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
       ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
       │ PostgreSQL  │  │    JWT      │  │   Ollama    │
       │  Database   │  │  Security   │  │     AI      │
       └─────────────┘  └─────────────┘  └─────────────┘
````

---

# 🛠️ Technology Stack

## Backend

| Technology         | Purpose                          |
| ------------------ | -------------------------------- |
| Java 25            | Backend programming language     |
| Spring Boot        | Backend application framework    |
| Spring Web         | REST API development             |
| Spring Data JPA    | Database access                  |
| Spring Security    | Authentication and authorization |
| PostgreSQL         | Relational database              |
| JWT                | Stateless authentication         |
| BCrypt             | Password hashing                 |
| Lombok             | Boilerplate reduction            |
| Jakarta Validation | Request validation               |
| Spring Mail        | Email and OTP functionality      |
| Maven              | Build and dependency management  |

## Frontend

| Technology   | Purpose                           |
| ------------ | --------------------------------- |
| React        | Frontend framework                |
| Vite         | Build tool and development server |
| React Router | Client-side routing               |
| Material UI  | UI components                     |
| Axios        | HTTP API communication            |
| Recharts     | Financial data visualization      |
| Day.js       | Date and time handling            |

## AI

| Technology  | Purpose                            |
| ----------- | ---------------------------------- |
| Ollama      | Local AI model integration         |
| AI Insights | Financial analysis                 |
| AI Chat     | Conversational financial assistant |

---

# 📁 Project Structure

```text
AI_EXPENSE_TRACKER/
│
├── expense-tracker-api/
│   ├── .mvn/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── Expense_calculator_UI/
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   └── utils/
│   ├── package.json
│   ├── vite.config.js
│   └── eslint.config.js
│
├── .gitignore
└── README.md
```

---

# 🗄️ Database

The application uses **PostgreSQL** as its relational database.

The main areas of the database include:

* Users
* User reminder settings
* Categories
* Expenses
* Income
* Budgets
* Notifications
* AI insights

The backend uses **Spring Data JPA and Hibernate** for object-relational mapping.

---

# 🔐 Authentication Flow

The application uses JWT-based authentication.

```text
User
  │
  │ Login
  ▼
React Frontend
  │
  │ POST /api/auth/login
  ▼
Spring Boot
  │
  ├── Validate credentials
  ├── Verify BCrypt password
  └── Generate JWT
  │
  ▼
React Frontend
  │
  ▼
Protected API Requests
  │
  ▼
JWT Authentication
  │
  ▼
Spring Security
  │
  ▼
Authorized Controller
```

---

# 🔑 Password Reset Flow

The forgot-password functionality uses OTP verification.

```text
User
  │
  ▼
Forgot Password
  │
  ▼
Enter Email
  │
  ▼
Spring Boot
  │
  ├── Generate OTP
  ├── Hash OTP
  ├── Store OTP and expiry
  └── Send OTP through email
  │
  ▼
User Enters OTP
  │
  ▼
OTP Verification
  │
  ├── Validate OTP
  ├── Check expiration
  └── Check attempts
  │
  ▼
Password Reset Token
  │
  ▼
Reset Password
  │
  ▼
BCrypt Password Hashing
  │
  ▼
Password Updated
```

---

# 🔔 Notification Architecture

```text
                     Backend
                        │
          ┌─────────────┼─────────────┐
          │             │             │
          ▼             ▼             ▼
    Budget Alerts   AI Insights   Expense Reminders
          │             │             │
          └─────────────┼─────────────┘
                        ▼
               Notification Service
                        │
                        ▼
                PostgreSQL Database
                        │
                        ▼
                  React Frontend
                        │
                        ▼
                 Notification Bell
```

---

# ⏱️ Scheduled Tasks

The backend uses scheduled services for automated operations such as:

* Expense reminder checks
* Daily AI insight generation
* Weekly AI insight generation
* Budget alert processing

Scheduled operations respect the user's configured notification and reminder settings.

---

# ⚙️ Installation & Setup

## Prerequisites

Install the following:

* Java 25
* PostgreSQL
* Node.js
* npm
* Git
* Ollama

Verify the installations:

```bash
java -version
node --version
npm --version
git --version
ollama --version
```

---

# 🗄️ PostgreSQL Setup

Create a PostgreSQL database for the application.

Example:

```sql
CREATE DATABASE ai_expense_tracker;
```

Configure the database connection in the backend's local configuration.

> Never commit database passwords or other sensitive credentials to GitHub.

---

# 🔐 Backend Configuration

The backend uses `application.properties` for environment-specific configuration.

Typical configuration areas include:

```properties
# Database
spring.datasource.url=...
spring.datasource.username=...
spring.datasource.password=...

# JWT
jwt.secret=...

# Mail
spring.mail.host=...
spring.mail.username=...
spring.mail.password=...

# Ollama
...
```

The actual configuration file should remain local and must not be committed to GitHub.

---

# 🚀 Running the Backend

Navigate to the backend directory:

```powershell
cd expense-tracker-api
```

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

---

# 🎨 Running the Frontend

Navigate to the frontend directory:

```bash
cd Expense_calculator_UI
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Vite will display the development URL in the terminal.

---

# 🧪 Testing

## Backend

From the backend directory:

### Windows

```powershell
.\mvnw.cmd clean test
```

### Linux / macOS

```bash
./mvnw clean test
```

## Frontend Linting

```bash
npm run lint
```

## Frontend Production Build

```bash
npm run build
```

---

# 📡 API Overview

The backend provides REST APIs for the application's major features.

Main API areas include:

```text
/api/auth
/api/users
/api/expenses
/api/income
/api/categories
/api/budgets
/api/notifications
/api/notification-settings
/api/ai
```

### Authentication APIs

```text
POST /api/auth/login
POST /api/auth/forgot-password
POST /api/auth/verify-otp
POST /api/auth/reset-password
```

### User APIs

```text
GET  /api/users/me
PUT  /api/users/me
PUT  /api/users/change-password
```

> API endpoints may evolve as development continues.

---

# 🔒 Security

The application implements:

* JWT-based authentication
* Spring Security
* BCrypt password hashing
* Protected API endpoints
* OTP verification
* OTP expiration
* OTP attempt handling
* Hashed password-reset tokens
* Single-use reset tokens
* Request validation
* User-specific data access
* Sensitive configuration exclusion

Sensitive values such as database passwords, JWT secrets, email credentials, and API keys should always be supplied through local or environment-specific configuration.


# 👨‍💻 Author

## Hariprasad N

**B.Tech – Computer Science and Business Systems**

Areas of interest:

* Full-Stack Development
* Backend Engineering
* Java & Spring Boot
* Python
* REST APIs
* PostgreSQL
* AI-integrated applications

