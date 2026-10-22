import {
  lazy,
  Suspense,
} from "react";

import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import {
  Box,
  CircularProgress,
} from "@mui/material";

import AppLayout from "./components/layouts/AppLayout";
import ProtectedRoute from "./components/ProtectedRoute";

// Public pages
const Login = lazy(() => import("./pages/Login"));
const Register = lazy(() => import("./pages/Register"));
const ForgotPassword = lazy(() => import("./pages/ForgetPassword"));
const VerifyOtp = lazy(() => import("./pages/VerifyOtp"));
const ResetPassword = lazy(() => import("./pages/ResetPassword"));
const VerifyEmail = lazy(() => import("./pages/verifyEmail"));

// Dashboard
const Dashboard = lazy(() => import("./pages/Dashboard"));

// Expense pages
const ExpenseList = lazy(() => import("./pages/ExpenseList"));
const EditExpense = lazy(() => import("./pages/EditExpense"));
const AddExpense = lazy(() => import("./pages/AddExpense"));

// Income pages
const IncomeList = lazy(() => import("./pages/IncomeList"));
const IncomeForm = lazy(() => import("./pages/IncomeForm"));

// Budget pages
const BudgetList = lazy(() => import("./pages/BudgetList"));
const BudgetForm = lazy(() => import("./pages/BudgetForm"));

// Other protected pages
const NotificationList = lazy(
  () => import("./pages/NotificationList")
);

const CategoryList = lazy(
  () => import("./pages/CategoryList")
);

const CategoryForm = lazy(
  () => import("./pages/CategoryForm")
);

const AiInsights = lazy(
  () => import("./pages/AiInsight")
);

const Settings = lazy(
  () => import("./pages/Settings")
);

// Displayed while a lazy-loaded page is loading.
function PageLoading() {
  return (
    <Box
      role="status"
      aria-label="Loading page"
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "60vh",
      }}
    >
      <CircularProgress />
    </Box>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageLoading />}>
        <Routes>
          {/* Public routes */}
          <Route
            path="/login"
            element={<Login />}
          />

          <Route
            path="/register"
            element={<Register />}
          />

          <Route
            path="/forgot-password"
            element={<ForgotPassword />}
          />

          <Route
            path="/verify-otp"
            element={<VerifyOtp />}
          />

          <Route path="/verify-email" element={<VerifyEmail />} />

          <Route
            path="/reset-password"
            element={<ResetPassword />}
          />

          {/* Protected routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route
                path="/dashboard"
                element={<Dashboard />}
              />

              <Route
                path="/expenses"
                element={<ExpenseList />}
              />

              <Route
                path="/expenses/:id/edit"
                element={<EditExpense />}
              />

              <Route
                path="/expenses/new"
                element={<AddExpense />}
              />

              <Route
                path="/income"
                element={<IncomeList />}
              />

              <Route
                path="/income/new"
                element={<IncomeForm />}
              />

              <Route
                path="/income/:id/edit"
                element={<IncomeForm />}
              />

              <Route
                path="/budgets"
                element={<BudgetList />}
              />

              <Route
                path="/budgets/new"
                element={<BudgetForm />}
              />

              <Route
                path="/budgets/:id/edit"
                element={<BudgetForm />}
              />

              <Route
                path="/notifications"
                element={<NotificationList />}
              />

              <Route
                path="/categories"
                element={<CategoryList />}
              />

              <Route
                path="/categories/new"
                element={<CategoryForm />}
              />

              <Route
                path="/categories/:id/edit"
                element={<CategoryForm />}
              />

              <Route
                path="/ai-insights"
                element={<AiInsights />}
              />

              <Route
                path="/settings"
                element={<Settings />}
              />
            </Route>
          </Route>

          {/* Default routes */}
          <Route
            path="/"
            element={
              <Navigate
                to="/dashboard"
                replace
              />
            }
          />

          <Route
            path="*"
            element={
              <Navigate
                to="/dashboard"
                replace
              />
            }
          />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

export default App;