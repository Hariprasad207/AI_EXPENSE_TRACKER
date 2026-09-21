import {
    useEffect,
    useState,
} from "react";

import {
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Alert,
    Typography,
    Grid,
    Chip,
    IconButton,
    Divider,
} from "@mui/material";

import {
    Add,
    Edit,
    Delete,
} from "@mui/icons-material";

import {
    useNavigate,
} from "react-router-dom";

import {
    getCategories,
    deleteCategory,
} from "../api/category";


function CategoryList() {

    const navigate = useNavigate();


    const [categories, setCategories] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    const loadCategories = async () => {

        try {

            setLoading(true);

            setError("");

            const data =
                await getCategories();

            setCategories(data);

        } catch (error) {

            setError(
                error.response?.data?.message ||
                "Failed to load categories"
            );

        } finally {

            setLoading(false);

        }

    };


    useEffect(() => {

        loadCategories();

    }, []);


    const handleDelete = async (id) => {

        const confirmed =
            window.confirm(
                "Are you sure you want to delete this category?"
            );

        if (!confirmed) {
            return;
        }


        try {

            await deleteCategory(id);


            setCategories(
                (previousCategories) =>
                    previousCategories.filter(
                        (category) =>
                            category.id !== id
                    )
            );

        } catch (error) {

            alert(
                error.response?.data?.message ||
                "Failed to delete category"
            );

        }

    };


    const expenseCategories =
        categories.filter(
            (category) =>
                category.type === "EXPENSE"
        );


    const incomeCategories =
        categories.filter(
            (category) =>
                category.type === "INCOME"
        );


    if (loading) {

        return (

            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    minHeight: "70vh",
                }}
            >

                <CircularProgress />

            </Box>

        );

    }


    return (

        <Box
            sx={{
                p: 3,
            }}
        >


            {/* =========================
                PAGE HEADER
            ========================== */}

            <Box
                sx={{
                    display: "flex",
                    justifyContent:
                        "space-between",
                    alignItems: "center",
                    mb: 3,
                }}
            >

                <Box>

                    <Typography
                        variant="h4"
                        fontWeight="bold"
                    >
                        Categories
                    </Typography>


                    <Typography
                        variant="body2"
                        color="text.secondary"
                    >
                        Manage your income and expense categories
                    </Typography>

                </Box>


                <Button
                    variant="contained"
                    startIcon={<Add />}
                    onClick={() =>
                        navigate("/categories/new")
                    }
                >
                    Add Category
                </Button>

            </Box>


            {/* =========================
                ERROR
            ========================== */}

            {error && (

                <Alert
                    severity="error"
                    sx={{ mb: 3 }}
                >
                    {error}
                </Alert>

            )}


            {/* =========================
                EXPENSE CATEGORIES
            ========================== */}

            <Typography
                variant="h5"
                fontWeight="bold"
                sx={{ mb: 2 }}
            >
                Expense Categories
            </Typography>


            <Grid
                container
                spacing={2}
                sx={{ mb: 4 }}
            >

                {expenseCategories.length === 0 && (

                    <Grid
                        size={{ xs: 12 }}
                    >

                        <Typography
                            color="text.secondary"
                        >
                            No expense categories found.
                        </Typography>

                    </Grid>

                )}


                {expenseCategories.map(
                    (category) => (

                        <Grid
                            size={{
                                xs: 12,
                                sm: 6,
                                md: 4,
                            }}
                            key={category.id}
                        >

                            <Card>

                                <CardContent>


                                    {/* HEADER */}

                                    <Box
                                        sx={{
                                            display: "flex",
                                            justifyContent:
                                                "space-between",
                                            alignItems:
                                                "center",
                                        }}
                                    >


                                        {/* CATEGORY INFO */}

                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems:
                                                    "center",
                                                gap: 1.5,
                                            }}
                                        >

                                            <Typography
                                                variant="h5"
                                            >
                                                {category.icon || "📁"}
                                            </Typography>


                                            <Box>

                                                <Typography
                                                    variant="h6"
                                                >
                                                    {category.name}
                                                </Typography>


                                                <Chip
                                                    label={
                                                        category.isCustom
                                                            ? "Custom"
                                                            : "Default"
                                                    }
                                                    size="small"
                                                    color={
                                                        category.isCustom
                                                            ? "primary"
                                                            : "default"
                                                    }
                                                />

                                            </Box>

                                        </Box>


                                        {/* ACTIONS */}

                                        {category.isCustom && (

                                            <Box>

                                                <IconButton
                                                    onClick={() =>
                                                        navigate(
                                                            `/categories/${category.id}/edit`
                                                        )
                                                    }
                                                >

                                                    <Edit />

                                                </IconButton>


                                                <IconButton
                                                    color="error"
                                                    onClick={() =>
                                                        handleDelete(
                                                            category.id
                                                        )
                                                    }
                                                >

                                                    <Delete />

                                                </IconButton>

                                            </Box>

                                        )}

                                    </Box>


                                </CardContent>

                            </Card>

                        </Grid>

                    )
                )}

            </Grid>


            <Divider
                sx={{ mb: 4 }}
            />


            {/* =========================
                INCOME CATEGORIES
            ========================== */}

            <Typography
                variant="h5"
                fontWeight="bold"
                sx={{ mb: 2 }}
            >
                Income Categories
            </Typography>


            <Grid
                container
                spacing={2}
            >

                {incomeCategories.length === 0 && (

                    <Grid
                        size={{ xs: 12 }}
                    >

                        <Typography
                            color="text.secondary"
                        >
                            No income categories found.
                        </Typography>

                    </Grid>

                )}


                {incomeCategories.map(
                    (category) => (

                        <Grid
                            size={{
                                xs: 12,
                                sm: 6,
                                md: 4,
                            }}
                            key={category.id}
                        >

                            <Card>

                                <CardContent>

                                    <Box
                                        sx={{
                                            display: "flex",
                                            justifyContent:
                                                "space-between",
                                            alignItems:
                                                "center",
                                        }}
                                    >


                                        {/* CATEGORY INFO */}

                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems:
                                                    "center",
                                                gap: 1.5,
                                            }}
                                        >

                                            <Typography
                                                variant="h5"
                                            >
                                                {category.icon || "💰"}
                                            </Typography>


                                            <Box>

                                                <Typography
                                                    variant="h6"
                                                >
                                                    {category.name}
                                                </Typography>


                                                <Chip
                                                    label={
                                                        category.isCustom
                                                            ? "Custom"
                                                            : "Default"
                                                    }
                                                    size="small"
                                                    color={
                                                        category.isCustom
                                                            ? "primary"
                                                            : "default"
                                                    }
                                                />

                                            </Box>

                                        </Box>


                                        {/* ACTIONS */}

                                        {category.isCustom && (

                                            <Box>

                                                <IconButton
                                                    onClick={() =>
                                                        navigate(
                                                            `/categories/${category.id}/edit`
                                                        )
                                                    }
                                                >

                                                    <Edit />

                                                </IconButton>


                                                <IconButton
                                                    color="error"
                                                    onClick={() =>
                                                        handleDelete(
                                                            category.id
                                                        )
                                                    }
                                                >

                                                    <Delete />

                                                </IconButton>

                                            </Box>

                                        )}

                                    </Box>

                                </CardContent>

                            </Card>

                        </Grid>

                    )
                )}

            </Grid>

        </Box>

    );

}


export default CategoryList;