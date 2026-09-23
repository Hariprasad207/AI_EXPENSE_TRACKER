import { useState } from "react";
import { Box, Button, Card, CardContent, TextField, Typography, Alert, } from "@mui/material";
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

    const handleChange = (event) => {

        setFormData({
            ...formData,
            [event.target.name]: event.target.value,
        });
    };

    const handleSubmit = async (event) => {

        event.preventDefault();

        try {
            setError("");
            await api.post(
                "/auth/register",
                formData
            );
            navigate("/login");
        } catch (error) {
            setError(
                error.response?.data?.message ||
                "Registration failed"
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
                backgroundColor: "#f5f5f5",
                padding: 2,
            }}
        >

            <Card sx={{ width: "100%", maxWidth: 400 }}>
                <CardContent sx={{ p: { xs: 2.5, sm: 4 } }}>
                    <Typography
                        variant="h4"
                        textAlign="center"
                        gutterBottom
                        sx={{ fontSize: { xs: "1.8rem", sm: "2.125rem" } }}
                    >
                        Create Account
                    </Typography>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}

                    <Box
                        component="form"
                        onSubmit={handleSubmit}
                    >
                        <TextField
                            fullWidth
                            label="Full Name"
                            name="fullName"
                            value={formData.fullName}
                            onChange={handleChange}
                            margin="normal"
                            required
                        />

                        <TextField
                            fullWidth
                            label="Email"
                            name="email"
                            type="email"
                            value={formData.email}
                            onChange={handleChange}
                            margin="normal"
                            required
                        />

                        <TextField
                            fullWidth
                            label="Password"
                            name="password"
                            type="password"
                            value={formData.password}
                            onChange={handleChange}
                            margin="normal"
                            required
                        />

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            size="large"
                            sx={{ mt: 3 }}
                        >
                            Register
                        </Button>

                    </Box>

                    <Typography
                        variant="body2"
                        textAlign="center"
                        sx={{ mt: 3 }}
                    >
                        Already have an account?{" "}
                        <Link to="/login">
                            Login
                        </Link>
                    </Typography>

                </CardContent>

            </Card>

        </Box>
    );
}

export default Register;
