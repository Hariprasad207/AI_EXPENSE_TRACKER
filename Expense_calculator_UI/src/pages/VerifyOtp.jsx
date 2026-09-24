import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
    Alert,
    Box,
    Button,
    Paper,
    TextField,
    Typography,
} from "@mui/material";
import { verifyOtp } from "../api/auth";

const VerifyOtp = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const email = location.state?.email || "";

    const [otp, setOtp] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const handleOtpChange = (event) => {
        const value = event.target.value;

        // Allow only numbers and maximum 6 digits
        if (/^\d{0,6}$/.test(value)) {
            setOtp(value);
        }

        setError("");
        setSuccess("");
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (!email) {
            setError(
                "Email information is missing. Please restart the password reset process."
            );
            return;
        }

        if (otp.length !== 6) {
            setError("Please enter the 6-digit OTP.");
            return;
        }

        try {
            setLoading(true);

            const response = await verifyOtp(email, otp);

            setSuccess(
                response.message || "OTP verified successfully."
            );

            /*
             * The backend returns the short-lived reset token.
             * Pass it to the Reset Password page using router state.
             */
            setTimeout(() => {
                navigate("/reset-password", {
                    state: {
                        email,
                        resetToken: response.resetToken,
                    },
                });
            }, 700);

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to verify OTP. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleBack = () => {
        navigate("/forgot-password");
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
                    Verify OTP
                </Typography>

                <Typography
                    variant="body2"
                    color="text.secondary"
                    textAlign="center"
                    sx={{ mb: 1 }}
                >
                    Enter the 6-digit verification code sent to:
                </Typography>

                <Typography
                    variant="body2"
                    fontWeight="bold"
                    textAlign="center"
                    sx={{ mb: 3 }}
                >
                    {email || "your email address"}
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
                        label="6-Digit OTP"
                        value={otp}
                        onChange={handleOtpChange}
                        disabled={loading}
                        inputProps={{
                            maxLength: 6,
                            inputMode: "numeric",
                            pattern: "[0-9]*",
                        }}
                        sx={{
                            mb: 2,
                            "& input": {
                                textAlign: "center",
                                letterSpacing: "8px",
                                fontSize: "24px",
                                fontWeight: "bold",
                            },
                        }}
                    />

                    <Button
                        fullWidth
                        type="submit"
                        variant="contained"
                        disabled={loading || otp.length !== 6}
                        sx={{ py: 1.3 }}
                    >
                        {loading ? "Verifying..." : "Verify OTP"}
                    </Button>

                    <Button
                        fullWidth
                        variant="text"
                        disabled={loading}
                        onClick={handleBack}
                        sx={{ mt: 1 }}
                    >
                        Back
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
};

export default VerifyOtp;