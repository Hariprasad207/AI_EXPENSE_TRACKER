import { useState } from "react";
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    TextField,
    Typography,
} from "@mui/material";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../api/axios";

function VerifyEmail() {
    const location = useLocation();
    const navigate = useNavigate();

    const [email, setEmail] = useState(location.state?.email || "");
    const [otp, setOtp] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);
    const [verified, setVerified] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        const normalizedEmail = email.trim().toLowerCase();
        const normalizedOtp = otp.trim();

        if (!normalizedEmail) {
            setError("Please enter your email address.");
            return;
        }

        if (!/^\d{6}$/.test(normalizedOtp)) {
            setError("Please enter the 6-digit OTP sent to your email.");
            return;
        }

        setLoading(true);

        try {
            const response = await api.post("/auth/verify-email", {
                email: normalizedEmail,
                otp: normalizedOtp,
            });

            setSuccess(
                response.data?.message ||
                "Your email has been verified successfully."
            );
            setVerified(true);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Email verification failed. Please check your OTP and try again."
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
                alignItems: "center",
                justifyContent: "center",
                backgroundColor: "#f5f5f5",
                p: 2,
            }}
        >
            <Card
                sx={{
                    width: "100%",
                    maxWidth: 420,
                    borderRadius: 3,
                    boxShadow: 3,
                }}
            >
                <CardContent sx={{ p: { xs: 2.5, sm: 4 } }}>
                    <Typography
                        variant="h4"
                        textAlign="center"
                        gutterBottom
                        sx={{
                            fontSize: {
                                xs: "1.8rem",
                                sm: "2.125rem",
                            },
                            fontWeight: 600,
                        }}
                    >
                        Verify Your Email
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        textAlign="center"
                        sx={{ mb: 3 }}
                    >
                        Enter the email address you registered with and the
                        6-digit verification code sent to your inbox.
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

                    {!verified ? (
                        <Box component="form" onSubmit={handleSubmit}>
                            <TextField
                                fullWidth
                                label="Email Address"
                                name="email"
                                type="email"
                                value={email}
                                onChange={(event) =>
                                    setEmail(event.target.value)
                                }
                                margin="normal"
                                autoComplete="email"
                                required
                                disabled={loading}
                            />

                            <TextField
                                fullWidth
                                label="6-digit OTP"
                                name="otp"
                                value={otp}
                                onChange={(event) => {
                                    const value = event.target.value;

                                    if (/^\d{0,6}$/.test(value)) {
                                        setOtp(value);
                                    }
                                }}
                                margin="normal"
                                placeholder="Enter your OTP"
                                autoComplete="one-time-code"
                                inputProps={{
                                    inputMode: "numeric",
                                    maxLength: 6,
                                }}
                                required
                                disabled={loading}
                            />

                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                size="large"
                                disabled={loading}
                                sx={{
                                    mt: 3,
                                    py: 1.3,
                                    textTransform: "none",
                                    fontSize: "1rem",
                                }}
                            >
                                {loading ? (
                                    <>
                                        <CircularProgress
                                            size={20}
                                            color="inherit"
                                            sx={{ mr: 1 }}
                                        />
                                        Verifying...
                                    </>
                                ) : (
                                    "Verify Email"
                                )}
                            </Button>
                        </Box>
                    ) : (
                        <Button
                            fullWidth
                            variant="contained"
                            size="large"
                            onClick={() => navigate("/login", { replace: true })}
                            sx={{
                                mt: 1,
                                py: 1.3,
                                textTransform: "none",
                            }}
                        >
                            Continue to Login
                        </Button>
                    )}

                    {!verified && (
                        <Typography
                            variant="body2"
                            textAlign="center"
                            sx={{ mt: 3 }}
                        >
                            Already verified?{" "}
                            <Link to="/login">Login</Link>
                        </Typography>
                    )}
                </CardContent>
            </Card>
        </Box>
    );
}

export default VerifyEmail;