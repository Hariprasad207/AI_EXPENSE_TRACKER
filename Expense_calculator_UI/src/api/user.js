import api from "./axios";


export const getCurrentUserProfile = async () => {

    const response =
        await api.get("/profile/me");

    return response.data;
};


export const updateCurrentUserProfile = async (
    profile
) => {

    const response =
        await api.put(
            "/profile/me",
            profile
        );

    return response.data;
};

export const changePassword = async (passwordData) => {
    const response = await api.put("/profile/change-password", passwordData);
    return response.data;
};