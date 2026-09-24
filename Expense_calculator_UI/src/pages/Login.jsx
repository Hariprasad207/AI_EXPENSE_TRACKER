import { useState } from "react";
import {
    Box,
    Button,
    Card,
    CardContent,
    TextField,
    Typography,
    Alert,
    Link as MuiLink,
    InputAdornment,
    IconButton,
} from "@mui/material";
import {
    Visibility,
    VisibilityOff,
    LockOutlined,
} from "@mui/icons-material";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

function Login() {

    const navigate = useNavigate();
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        email: "",
        password: "",
    });

    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);

    const handleChange = (event) => {
        setFormData({
            ...formData,
            [event.target.name]: event.target.value,
        });

        setError("");
    };

    const handleSubmit = async (event) => {

        event.preventDefault();

        try {

            setError("");

            const response = await api.post(
                "/auth/login",
                formData
            );

            login(response.data.token);

            navigate("/dashboard");

        } catch (error) {

            setError(
                error.response?.data?.message ||
                "Login failed"
            );
        }
    };

    return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                background: "linear-gradient(135deg, #f5f7fa 0%, #e8eef7 100%)",
                padding: 2,
            }}
        >

            <Card
                elevation={0}
                sx={{
                    width: "100%",
                    maxWidth: 430,
                    borderRadius: 4,
                    border: "1px solid",
                    borderColor: "divider",
                    boxShadow: "0 12px 40px rgba(0, 0, 0, 0.08)",
                    overflow: "hidden",
                }}
            >

                <CardContent
                    sx={{
                        p: {
                            xs: 3,
                            sm: 4.5,
                        },
                    }}
                >

                    {/* Header */}
                    <Box
                        sx={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            mb: 3,
                        }}
                    >

                        <Box
                            sx={{
                                width: 58,
                                height: 58,
                                borderRadius: "50%",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                backgroundColor: "primary.main",
                                color: "white",
                                mb: 2,
                            }}
                        >
                            <LockOutlined fontSize="medium" />
                        </Box>

                        <Typography
                            variant="h4"
                            sx={{
                                fontWeight: 700,
                                textAlign: "center",
                                fontSize: {
                                    xs: "1.75rem",
                                    sm: "2rem",
                                },
                            }}
                        >
                            Expense Tracker
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                mt: 0.75,
                                textAlign: "center",
                            }}
                        >
                            Login to your account
                        </Typography>

                    </Box>

                    {/* Error */}
                    {error && (
                        <Alert
                            severity="error"
                            sx={{
                                mb: 2,
                                borderRadius: 2,
                            }}
                        >
                            {error}
                        </Alert>
                    )}

                    {/* Login Form */}
                    <Box
                        component="form"
                        onSubmit={handleSubmit}
                    >

                        <TextField
                            fullWidth
                            label="Email"
                            name="email"
                            type="email"
                            value={formData.email}
                            onChange={handleChange}
                            margin="normal"
                            required
                            autoComplete="email"
                            sx={{
                                "& .MuiOutlinedInput-root": {
                                    borderRadius: 2,
                                },
                            }}
                        />

                        <TextField
                            fullWidth
                            label="Password"
                            name="password"
                            type={showPassword ? "text" : "password"}
                            value={formData.password}
                            onChange={handleChange}
                            margin="normal"
                            required
                            autoComplete="current-password"
                            sx={{
                                "& .MuiOutlinedInput-root": {
                                    borderRadius: 2,
                                },
                            }}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() =>
                                                setShowPassword(
                                                    (previous) => !previous
                                                )
                                            }
                                            edge="end"
                                            aria-label={
                                                showPassword
                                                    ? "Hide password"
                                                    : "Show password"
                                            }
                                        >
                                            {showPassword ? (
                                                <VisibilityOff />
                                            ) : (
                                                <Visibility />
                                            )}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />

                        {/* Forgot Password */}
                        <Box
                            sx={{
                                display: "flex",
                                justifyContent: "flex-end",
                                mt: 0.5,
                            }}
                        >
                            <MuiLink
                                component="button"
                                type="button"
                                underline="hover"
                                onClick={() =>
                                    navigate("/forgot-password")
                                }
                                sx={{
                                    border: 0,
                                    background: "none",
                                    padding: 0,
                                    cursor: "pointer",
                                    fontSize: "0.875rem",
                                    fontWeight: 500,
                                    color: "primary.main",
                                }}
                            >
                                Forgot Password?
                            </MuiLink>
                        </Box>

                        {/* Login Button */}
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            size="large"
                            sx={{
                                mt: 3,
                                py: 1.35,
                                borderRadius: 2,
                                fontSize: "1rem",
                                fontWeight: 600,
                                textTransform: "none",
                                boxShadow: "none",
                                "&:hover": {
                                    boxShadow:
                                        "0 6px 16px rgba(25, 118, 210, 0.25)",
                                },
                            }}
                        >
                            Login
                        </Button>

                    </Box>

                    {/* Register */}
                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mt: 3,
                            textAlign: "center",
                        }}
                    >
                        Don't have an account?{" "}

                        <Link
                            to="/register"
                            style={{
                                textDecoration: "none",
                                fontWeight: 600,
                            }}
                        >
                            Register
                        </Link>
                    </Typography>

                </CardContent>

            </Card>

        </Box>
    );
}

export default Login;