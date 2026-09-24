import api from "./axios";

export const forgotPassword = async (email) => {
    const response = await api.post("/auth/forgot-password", {
        email,
    });

    return response.data;
};

export const verifyOtp = async (email, otp) => {
    const response = await api.post("/auth/verify-password", {
        email,
        otp,
    });

    return response.data;
};

export const resetPassword = async (
    email,
    resetToken,
    newPassword,
    confirmPassword
) => {
    const response = await api.post("/auth/reset-password", {
        email,
        resetToken,
        newPassword,
        confirmPassword,
    });

    return response.data;
};