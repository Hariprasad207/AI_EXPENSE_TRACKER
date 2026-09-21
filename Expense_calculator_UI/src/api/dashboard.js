import api from "./axios";

export const getDashboardSummary = async (month, year) => {
    const response = await api.get("/dashboard/summary", {
        params: {
            month,
            year,
        },
    });

    return response.data;
};