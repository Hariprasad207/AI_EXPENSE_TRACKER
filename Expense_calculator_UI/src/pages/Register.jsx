import { useState } from "react";
import {
    Box,
    Button,
    Card,
    CardContent,
    TextField,
    Typography,
    Alert,
    InputAdornment,
    IconButton,
    CircularProgress,
} from "@mui/material";
import { Visibility, VisibilityOff } from "@mui/icons-material";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";

function Register() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        fullName: "",
        email: "",
        password: "",
    });

    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");
        setLoading(true);

        try {
            const email = formData.email.trim().toLowerCase();

            await api.post("/auth/register", {
                fullName: formData.fullName.trim(),
                email,
                password: formData.password,
            });

            navigate("/verify-email", {
                replace: true,
                state: { email },
            });
        } catch (error) {
            setError(
                error.response?.data?.message ||
                error.response?.data?.error ||
                "Registration failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    const togglePasswordVisibility = () => {
        setShowPassword((previous) => !previous);
    };

    return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                backgroundColor: "#f5f5f5",
                padding: 2,
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
                        Create Account
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        textAlign="center"
                        sx={{ mb: 2.5 }}
                    >
                        Register to get started with your Expense Tracker.
                        You'll verify your email before logging in.
                    </Typography>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}

                    <Box component="form" onSubmit={handleSubmit}>
                        <TextField
                            fullWidth
                            label="Full Name"
                            name="fullName"
                            value={formData.fullName}
                            onChange={handleChange}
                            margin="normal"
                            autoComplete="name"
                            required
                            disabled={loading}
                        />

                        <TextField
                            fullWidth
                            label="Email"
                            name="email"
                            type="email"
                            value={formData.email}
                            onChange={handleChange}
                            margin="normal"
                            autoComplete="email"
                            required
                            disabled={loading}
                        />

                        <TextField
                            fullWidth
                            label="Password"
                            name="password"
                            type={showPassword ? "text" : "password"}
                            value={formData.password}
                            onChange={handleChange}
                            margin="normal"
                            autoComplete="new-password"
                            required
                            disabled={loading}
                            slotProps={{
                                input: {
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton
                                                type="button"
                                                onClick={togglePasswordVisibility}
                                                edge="end"
                                                aria-label={
                                                    showPassword
                                                        ? "Hide password"
                                                        : "Show password"
                                                }
                                                aria-pressed={showPassword}
                                                disabled={loading}
                                            >
                                                {showPassword ? (
                                                    <VisibilityOff />
                                                ) : (
                                                    <Visibility />
                                                )}
                                            </IconButton>
                                        </InputAdornment>
                                    ),
                                },
                            }}
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
                                    Creating account...
                                </>
                            ) : (
                                "Create Account"
                            )}
                        </Button>
                    </Box>

                    <Typography
                        variant="body2"
                        textAlign="center"
                        sx={{ mt: 3 }}
                    >
                        Already have an account?{" "}
                        <Link to="/login">Login</Link>
                    </Typography>
                </CardContent>
            </Card>
        </Box>
    );
}

export default Register;