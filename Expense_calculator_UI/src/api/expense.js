import api from "./axios";

export const getExpenses = async ({ page = 0, size = 10, sort = "expenseDate,desc", categoryId, paymentMode, fromDate, toDate, } = {}) => {

    const params = {
        page,
        size,
        sort,
        categoryId,
        paymentMode,
        fromDate,
        toDate,
    };

    const response = await api.get("/expenses", {
        params,
    });

    return response.data;
};

export const getExpenseById = async (id) => {
    const response = await api.get(`/expenses/${id}`);
    return response.data;
}

export const createExpense = async (expenseData) => {
    const response = await api.post("/expenses", expenseData);
    return response.data;
}

export const updateExpense = async (id, expenseData) => {
    const response = await api.put(`/expenses/${id}`, expenseData);
    return response.data;
}

export const deleteExpense = async (id) => {
    const response = await api.delete(`/expenses/${id}`);
    return response.data;
}