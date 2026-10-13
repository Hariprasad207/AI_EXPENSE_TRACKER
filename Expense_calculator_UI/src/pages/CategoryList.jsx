import { useEffect, useState } from "react";

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
    Restaurant,
    ShoppingCart,
    DirectionsCar,
    LocalGasStation,
    ShoppingBag,
    Movie,
    ReceiptLong,
    Home,
    MedicalServices,
    School,
    Flight,
    FitnessCenter,
    Spa,
    Subscriptions,
    Security,
    Payments,
    Work,
    Business,
    TrendingUp,
    AccountBalance,
    CardGiftcard,
    Redeem,
    AttachMoney,
    Category as DefaultCategoryIcon,
} from "@mui/icons-material";

import { useNavigate } from "react-router-dom";

import {
    getCategories,
    deleteCategory,
} from "../api/category";

// Map the icon identifiers stored in PostgreSQL to Material UI icons.
const CATEGORY_ICONS = {
    restaurant: Restaurant,
    shopping_cart: ShoppingCart,
    directions_car: DirectionsCar,
    local_gas_station: LocalGasStation,
    shopping_bag: ShoppingBag,
    movie: Movie,
    receipt_long: ReceiptLong,
    home: Home,
    medical_services: MedicalServices,
    school: School,
    flight: Flight,
    fitness_center: FitnessCenter,
    spa: Spa,
    subscriptions: Subscriptions,
    security: Security,
    payments: Payments,
    work: Work,
    business: Business,
    trending_up: TrendingUp,
    account_balance: AccountBalance,
    card_giftcard: CardGiftcard,
    redeem: Redeem,
    attach_money: AttachMoney,
};

// Reusable icon component with a fallback for unknown identifiers.
function CategoryIcon({ icon, color = "action" }) {
    const IconComponent =
        CATEGORY_ICONS[icon] || DefaultCategoryIcon;

    return (
        <IconComponent
            fontSize="large"
            color={color}
            aria-hidden="true"
        />
    );
}

function CategoryList() {
    const navigate = useNavigate();

    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadCategories = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getCategories();
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
        const timeoutId = window.setTimeout(() => {
            void loadCategories();
        }, 0);

        return () => window.clearTimeout(timeoutId);
    }, []);

    const handleDelete = async (id) => {
        const confirmed = window.confirm(
            "Are you sure you want to delete this category?"
        );

        if (!confirmed) {
            return;
        }

        try {
            await deleteCategory(id);

            setCategories((previousCategories) =>
                previousCategories.filter(
                    (category) => category.id !== id
                )
            );
        } catch (error) {
            window.alert(
                error.response?.data?.message ||
                "Failed to delete category"
            );
        }
    };

    const expenseCategories = categories.filter(
        (category) => category.type === "EXPENSE"
    );

    const incomeCategories = categories.filter(
        (category) => category.type === "INCOME"
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
        <Box sx={{ p: 3 }}>
            {/* Page header */}
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 3,
                    gap: 2,
                }}
            >
                <Box>
                    <Typography variant="h4" fontWeight="bold">
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
                    onClick={() => navigate("/categories/new")}
                >
                    Add Category
                </Button>
            </Box>

            {/* Error message */}
            {error && (
                <Alert severity="error" sx={{ mb: 3 }}>
                    {error}
                </Alert>
            )}

            {/* Expense categories */}
            <Typography
                variant="h5"
                fontWeight="bold"
                sx={{ mb: 2 }}
            >
                Expense Categories
            </Typography>

            <Grid container spacing={2} sx={{ mb: 4 }}>
                {expenseCategories.length === 0 && (
                    <Grid size={{ xs: 12 }}>
                        <Typography color="text.secondary">
                            No expense categories found.
                        </Typography>
                    </Grid>
                )}

                {expenseCategories.map((category) => (
                    <Grid
                        size={{ xs: 12, sm: 6, md: 4 }}
                        key={category.id}
                    >
                        <Card>
                            <CardContent>
                                <Box
                                    sx={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        alignItems: "center",
                                        gap: 2,
                                    }}
                                >
                                    {/* Category information */}
                                    <Box
                                        sx={{
                                            display: "flex",
                                            alignItems: "center",
                                            gap: 1.5,
                                            minWidth: 0,
                                        }}
                                    >
                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems: "center",
                                                justifyContent: "center",
                                                flexShrink: 0,
                                            }}
                                        >
                                            <CategoryIcon
                                                icon={category.icon}
                                            />
                                        </Box>

                                        <Box sx={{ minWidth: 0 }}>
                                            <Typography
                                                variant="h6"
                                                sx={{
                                                    overflowWrap: "anywhere",
                                                }}
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

                                    {/* Actions for custom categories */}
                                    {category.isCustom && (
                                        <Box
                                            sx={{
                                                display: "flex",
                                                flexShrink: 0,
                                            }}
                                        >
                                            <IconButton
                                                aria-label={`Edit ${category.name}`}
                                                onClick={() =>
                                                    navigate(
                                                        `/categories/${category.id}/edit`
                                                    )
                                                }
                                            >
                                                <Edit />
                                            </IconButton>

                                            <IconButton
                                                aria-label={`Delete ${category.name}`}
                                                color="error"
                                                onClick={() =>
                                                    handleDelete(category.id)
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
                ))}
            </Grid>

            <Divider sx={{ mb: 4 }} />

            {/* Income categories */}
            <Typography
                variant="h5"
                fontWeight="bold"
                sx={{ mb: 2 }}
            >
                Income Categories
            </Typography>

            <Grid container spacing={2}>
                {incomeCategories.length === 0 && (
                    <Grid size={{ xs: 12 }}>
                        <Typography color="text.secondary">
                            No income categories found.
                        </Typography>
                    </Grid>
                )}

                {incomeCategories.map((category) => (
                    <Grid
                        size={{ xs: 12, sm: 6, md: 4 }}
                        key={category.id}
                    >
                        <Card>
                            <CardContent>
                                <Box
                                    sx={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        alignItems: "center",
                                        gap: 2,
                                    }}
                                >
                                    {/* Category information */}
                                    <Box
                                        sx={{
                                            display: "flex",
                                            alignItems: "center",
                                            gap: 1.5,
                                            minWidth: 0,
                                        }}
                                    >
                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems: "center",
                                                justifyContent: "center",
                                                flexShrink: 0,
                                            }}
                                        >
                                            <CategoryIcon
                                                icon={category.icon}
                                                color="success"
                                            />
                                        </Box>

                                        <Box sx={{ minWidth: 0 }}>
                                            <Typography
                                                variant="h6"
                                                sx={{
                                                    overflowWrap: "anywhere",
                                                }}
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

                                    {/* Actions for custom categories */}
                                    {category.isCustom && (
                                        <Box
                                            sx={{
                                                display: "flex",
                                                flexShrink: 0,
                                            }}
                                        >
                                            <IconButton
                                                aria-label={`Edit ${category.name}`}
                                                onClick={() =>
                                                    navigate(
                                                        `/categories/${category.id}/edit`
                                                    )
                                                }
                                            >
                                                <Edit />
                                            </IconButton>

                                            <IconButton
                                                aria-label={`Delete ${category.name}`}
                                                color="error"
                                                onClick={() =>
                                                    handleDelete(category.id)
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
                ))}
            </Grid>
        </Box>
    );
}

export default CategoryList;
