import api from "./axios";


export const createBudget = async (data) => {
    const response = await api.post("/budget", data);
    return response.data;
};


export const getBudgetById = async (id) => {
    const response = await api.get(`/budget/${id}`);
    return response.data;
};


export const updateBudget = async (id, data) => {
    const response = await api.put(`/budget/${id}`, data);
    return response.data;
};


export const deleteBudget = async (id) => {
    await api.delete(`/budget/${id}`);
};


export const getBudgets = async ({ month, year } = {}) => {

    const response = await api.get("/budget", {
        params: {
            ...(month && {
                month,
            }),

            ...(year && {
                year,
            }),
        },
    });

    return response.data;
};

export const getBudgetProgress = async (id) => {
    const response = await api.get(`/budget/${id}/progress`);
    return response.data;
};  