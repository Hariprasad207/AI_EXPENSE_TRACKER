import api from "./axios";


export const sendAiChatMessage = async (
    message,
    conversationId = null
) => {

    const response =
        await api.post(
            "/ai-chat",
            {
                message,
                conversationId,
            }
        );

    return response.data;
};


export const getAiChatHistory = async () => {

    const response =
        await api.get("/ai-chat");

    return response.data;
};


export const getAiConversation = async (
    conversationId
) => {

    const response =
        await api.get(
            `/ai-chat/${conversationId}`
        );

    return response.data;
};


export const clearAiChatHistory = async () => {

    await api.delete(
        "/ai-chat"
    );

};


export const clearAiConversation = async (
    conversationId
) => {

    await api.delete(
        `/ai-chat/${conversationId}`
    );

};


export const getFinancialContext = async () => {

    const response =
        await api.get(
            "/ai-chat/financial-context"
        );

    return response.data;
};