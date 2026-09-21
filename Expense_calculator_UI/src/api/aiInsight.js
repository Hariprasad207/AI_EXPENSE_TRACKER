import api from "./axios";

export const getAiInsights = async () => {
    const response = await api.get("/ai-insights");
    return response.data;
};

export const getUnreadAiInsights = async () => {
    const response = await api.get("/ai-insights/unread");
    return response.data;
};

export const getUnreadAiInsightCount = async () => {
    const response = await api.get("/ai-insights/unread-count");
    return response.data;
};

export const analyzeAiInsights = async () => {
    const response = await api.post("/ai-insights/analyze");
    return response.data;
};

export const markAiInsightAsRead = async (id) => {
    const response = await api.put(`/ai-insights/${id}/read`);
    return response.data;
};

export const deleteAiInsight = async (id) => {
    await api.delete(`/ai-insights/${id}`);
};

export const markAllAiInsightsAsRead = async () => {
    // Your current backend controller does not have this endpoint yet.
    // We will add it later if needed.
};