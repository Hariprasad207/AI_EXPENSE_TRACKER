import api from "./axios";


// Get all notifications
export const getNotifications = async () => {
    const response = await api.get("/notifications");
    return response.data;
};


// Get unread notification count
export const getUnreadNotificationCount = async () => {
    const response = await api.get(
        "/notifications/unreaded-count"
    );

    return response.data;
};


// Mark one notification as read
export const markNotificationAsRead = async (id) => {
    const response = await api.put(
        `/notifications/${id}/read`
    );

    return response.data;
};


// Mark all notifications as read
export const markAllNotificationsAsRead = async () => {
    await api.put(
        "/notifications/read-all"
    );
};


// Delete notification
export const deleteNotification = async (id) => {
    await api.delete(
        `/notifications/${id}`
    );
};