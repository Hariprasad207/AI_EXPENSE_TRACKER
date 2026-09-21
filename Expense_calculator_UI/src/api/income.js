import api from "./axios";


export const createIncome = async (data) => {
    const response = await api.post("/income", data);
    return response.data;
};


export const getIncomeById = async (id) => {
    const response = await api.get(`/income/${id}`);
    return response.data;
};


export const updateIncome = async (id, data) => {
    const response = await api.put(`/income/${id}`, data);
    return response.data;
};


export const deleteIncome = async (id) => {
    await api.delete(`/income/${id}`);
};


export const getIncome = async ({ page = 0, size = 10, categoryId, fromDate, toDate, } = {}) => {

    const response = await api.get("/income", {
        params: {
            page,
            size,

            ...(categoryId && {
                categoryId,
            }),

            ...(fromDate && {
                fromDate,
            }),

            ...(toDate && {
                toDate,
            }),
        },
    });

    return response.data;
};