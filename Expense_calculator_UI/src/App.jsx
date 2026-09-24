import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import AppLayout from "./components/layouts/AppLayout";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";

import Dashboard from "./pages/Dashboard";

import ExpenseList from "./pages/ExpenseList";
import EditExpense from "./pages/EditExpense";
import AddExpense from "./pages/AddExpense";

import IncomeList from "./pages/IncomeList";
import IncomeForm from "./pages/IncomeForm";

import BudgetList from "./pages/BudgetList";
import BudgetForm from "./pages/BudgetForm";

import NotificationList from "./pages/NotificationList";

import CategoryList from "./pages/CategoryList";
import CategoryForm from "./pages/CategoryForm";

import AiInsights from "./pages/AiInsight";

import Settings from "./pages/Settings";

import ForgotPassword from "./pages/ForgetPassword";
import VerifyOtp from "./pages/VerifyOtp";
import ResetPassword from "./pages/ResetPassword";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* =========================
            PUBLIC ROUTES
        ========================== */}

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

        <Route
          path="/reset-password"
          element={<ResetPassword />}
        />


        {/* =========================
            PROTECTED ROUTES
        ========================== */}

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


        {/* =========================
            DEFAULT ROUTES
        ========================== */}

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
    </BrowserRouter>
  );
}

export default App;