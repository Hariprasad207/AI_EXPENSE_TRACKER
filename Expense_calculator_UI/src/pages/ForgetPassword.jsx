import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    Box,
    Button,
    TextField,
    Typography,
    Paper,
    Alert,
} from "@mui/material";
import { forgotPassword } from "../api/auth";

const ForgotPassword = () => {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (!email.trim()) {
            setError("Please enter your email address.");
            return;
        }

        try {
            setLoading(true);

            const response = await forgotPassword(email.trim());

            setSuccess(
                response.message ||
                "OTP has been sent to your email address."
            );

            // Move to OTP verification after the request succeeds.
            setTimeout(() => {
                navigate("/verify-otp", {
                    state: {
                        email: email.trim(),
                    },
                });
            }, 1000);

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to process your request."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                padding: 2,
            }}
        >
            <Paper
                elevation={4}
                sx={{
                    width: "100%",
                    maxWidth: 450,
                    padding: 4,
                    borderRadius: 3,
                }}
            >
                <Typography
                    variant="h4"
                    fontWeight="bold"
                    textAlign="center"
                    gutterBottom
                >
                    Forgot Password
                </Typography>

                <Typography
                    variant="body2"
                    color="text.secondary"
                    textAlign="center"
                    sx={{ mb: 3 }}
                >
                    Enter your registered email address and we'll send you
                    a verification code.
                </Typography>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                {success && (
                    <Alert severity="success" sx={{ mb: 2 }}>
                        {success}
                    </Alert>
                )}

                <Box component="form" onSubmit={handleSubmit}>
                    <TextField
                        fullWidth
                        label="Email Address"
                        type="email"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        disabled={loading}
                        autoComplete="email"
                        sx={{ mb: 2 }}
                    />

                    <Button
                        fullWidth
                        type="submit"
                        variant="contained"
                        disabled={loading}
                        sx={{ py: 1.3 }}
                    >
                        {loading ? "Sending OTP..." : "Send OTP"}
                    </Button>

                    <Button
                        fullWidth
                        variant="text"
                        disabled={loading}
                        onClick={() => navigate("/login")}
                        sx={{ mt: 1 }}
                    >
                        Back to Login
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
};

export default ForgotPassword;