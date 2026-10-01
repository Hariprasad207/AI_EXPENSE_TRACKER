import api from "./axios";

export const getNotificationSettings = async () => {
    const response = await api.get("/notifications/settings");
    return response.data;
};

export const updateNotificationSettings = async (settings) => {
    const response = await api.put("/notifications/settings", settings);
    return response.data;
};