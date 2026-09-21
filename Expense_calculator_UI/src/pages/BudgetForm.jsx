import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
    createBudget,
    getBudgetById,
    updateBudget,
} from "../api/budget";

import { getCategories } from "../api/category";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from "@mui/material";


const months = [
    { value: 1, label: "January" },
    { value: 2, label: "February" },
    { value: 3, label: "March" },
    { value: 4, label: "April" },
    { value: 5, label: "May" },
    { value: 6, label: "June" },
    { value: 7, label: "July" },
    { value: 8, label: "August" },
    { value: 9, label: "September" },
    { value: 10, label: "October" },
    { value: 11, label: "November" },
    { value: 12, label: "December" },
];


function BudgetForm() {

    const navigate = useNavigate();
    const { id } = useParams();

    const isEditMode = Boolean(id);

    const currentDate = new Date();

    const [formData, setFormData] = useState({
        categoryId: "",
        amount: "",
        month: currentDate.getMonth() + 1,
        year: currentDate.getFullYear(),
    });

    const [categories, setCategories] = useState([]);

    const [loading, setLoading] = useState(isEditMode);
    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");


    // ---------------------------------------
    // Load Expense Categories
    // ---------------------------------------

    useEffect(() => {

        const loadCategories = async () => {

            try {

                const data = await getCategories();

                const expenseCategories = data.filter(
                    (category) =>
                        category.type === "EXPENSE"
                );

                setCategories(expenseCategories);

            } catch (err) {

                console.error(
                    "Failed to load categories:",
                    err
                );

                setError(
                    err.response?.data?.message ||
                    "Failed to load categories"
                );
            }
        };

        loadCategories();

    }, []);


    // ---------------------------------------
    // Load Budget When Editing
    // ---------------------------------------

    useEffect(() => {

        if (!isEditMode) {
            return;
        }

        const loadBudget = async () => {

            try {

                setLoading(true);
                setError("");

                const data = await getBudgetById(id);

                setFormData({
                    categoryId:
                        data.categoryId ?? "",
                    amount: data.amount ?? "",
                    month: data.month,
                    year: data.year,
                });

            } catch (err) {

                console.error(
                    "Failed to load budget:",
                    err
                );

                setError(
                    err.response?.data?.message ||
                    "Failed to load budget"
                );

            } finally {

                setLoading(false);
            }
        };

        loadBudget();

    }, [id, isEditMode]);


    // ---------------------------------------
    // Handle Input Change
    // ---------------------------------------

    const handleChange = (event) => {

        const { name, value } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };


    // ---------------------------------------
    // Submit
    // ---------------------------------------

    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");

        // Basic frontend validation

        if (
            formData.amount === "" ||
            Number(formData.amount) <= 0
        ) {
            setError(
                "Amount must be greater than 0"
            );

            return;
        }

        if (
            !formData.month ||
            Number(formData.month) < 1 ||
            Number(formData.month) > 12
        ) {
            setError("Please select a valid month");

            return;
        }

        if (
            !formData.year ||
            Number(formData.year) < 2000
        ) {
            setError("Please enter a valid year");

            return;
        }


        const requestData = {
            categoryId:
                formData.categoryId === ""
                    ? null
                    : Number(formData.categoryId),

            amount: Number(formData.amount),

            month: Number(formData.month),

            year: Number(formData.year),
        };


        try {

            setSaving(true);

            if (isEditMode) {

                await updateBudget(
                    id,
                    requestData
                );

            } else {

                await createBudget(
                    requestData
                );
            }

            navigate("/budgets");

        } catch (err) {

            console.error(
                "Failed to save budget:",
                err
            );

            setError(
                err.response?.data?.message ||
                "Failed to save budget"
            );

        } finally {

            setSaving(false);
        }
    };


    // ---------------------------------------
    // Loading
    // ---------------------------------------

    if (loading) {

        return (
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    mt: 5,
                }}
            >
                <CircularProgress />
            </Box>
        );
    }


    // ---------------------------------------
    // UI
    // ---------------------------------------

    return (
        <Box>

            <Typography
                variant="h4"
                fontWeight={600}
                sx={{ mb: 3 }}
            >
                {isEditMode
                    ? "Edit Budget"
                    : "Add Budget"}
            </Typography>


            <Card>

                <CardContent>

                    <Box
                        component="form"
                        onSubmit={handleSubmit}
                    >

                        <Stack spacing={3}>

                            {error && (
                                <Alert severity="error">
                                    {error}
                                </Alert>
                            )}


                            {/* Category */}

                            <Box>

                                <Typography
                                    variant="body2"
                                    sx={{ mb: 1 }}
                                >
                                    Category
                                </Typography>

                                <Select
                                    fullWidth
                                    name="categoryId"
                                    value={
                                        formData.categoryId
                                    }
                                    onChange={
                                        handleChange
                                    }
                                    displayEmpty
                                >

                                    <MenuItem value="">
                                        Overall Budget
                                    </MenuItem>


                                    {categories.map(
                                        (category) => (
                                            <MenuItem
                                                key={
                                                    category.id
                                                }
                                                value={
                                                    category.id
                                                }
                                            >
                                                {
                                                    category.name
                                                }
                                            </MenuItem>
                                        )
                                    )}

                                </Select>

                            </Box>


                            {/* Amount */}

                            <TextField
                                label="Amount"
                                name="amount"
                                type="number"
                                value={
                                    formData.amount
                                }
                                onChange={
                                    handleChange
                                }
                                fullWidth
                                required
                                inputProps={{
                                    min: 0.01,
                                    step: 0.01,
                                }}
                            />


                            {/* Month */}

                            <Box>

                                <Typography
                                    variant="body2"
                                    sx={{ mb: 1 }}
                                >
                                    Month
                                </Typography>

                                <Select
                                    fullWidth
                                    name="month"
                                    value={
                                        formData.month
                                    }
                                    onChange={
                                        handleChange
                                    }
                                >

                                    {months.map(
                                        (month) => (
                                            <MenuItem
                                                key={
                                                    month.value
                                                }
                                                value={
                                                    month.value
                                                }
                                            >
                                                {
                                                    month.label
                                                }
                                            </MenuItem>
                                        )
                                    )}

                                </Select>

                            </Box>


                            {/* Year */}

                            <TextField
                                label="Year"
                                name="year"
                                type="number"
                                value={
                                    formData.year
                                }
                                onChange={
                                    handleChange
                                }
                                fullWidth
                                required
                                inputProps={{
                                    min: 2000,
                                }}
                            />


                            {/* Buttons */}

                            <Stack
                                direction="row"
                                spacing={2}
                                justifyContent="flex-end"
                            >

                                <Button
                                    variant="outlined"
                                    onClick={() =>
                                        navigate(
                                            "/budgets"
                                        )
                                    }
                                    disabled={saving}
                                >
                                    Cancel
                                </Button>


                                <Button
                                    type="submit"
                                    variant="contained"
                                    disabled={saving}
                                >
                                    {saving
                                        ? "Saving..."
                                        : isEditMode
                                            ? "Update Budget"
                                            : "Save Budget"}
                                </Button>

                            </Stack>

                        </Stack>

                    </Box>

                </CardContent>

            </Card>

        </Box>
    );
}


export default BudgetForm;