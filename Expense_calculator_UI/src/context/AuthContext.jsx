import { useState } from "react";
import { AuthContext } from "./AuthContextStore.js";

export const AuthProvider = ({ children }) => {

    const [token, setToken] = useState(
        localStorage.getItem("token")
    );

    const login = (token) => {
        localStorage.setItem("token", token);
        setToken(token);
    };

    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
    };

    const isAuthenticated = !!token;

    return (
        <AuthContext.Provider
            value={{
                token,
                login,
                logout,
                isAuthenticated,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

