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


function App() {

  return (
    <BrowserRouter>

      <Routes>

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

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

          </Route>

        </Route>


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