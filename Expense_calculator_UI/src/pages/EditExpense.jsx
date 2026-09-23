import { useEffect, useState } from "react";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    TextField,
    Typography,
} from "@mui/material";

import { useNavigate, useParams } from "react-router-dom";

import {
    getExpenseById,
    updateExpense,
} from "../api/expense";

import { getCategories } from "../api/category";


function EditExpense() {

    const { id } = useParams();

    const navigate = useNavigate();

    const [categories, setCategories] = useState([]);

    const [loading, setLoading] = useState(true);

    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");

    const [formData, setFormData] = useState({
        categoryId: "",
        amount: "",
        paymentMode: "",
        expenseDate: "",
        title: "",
        description: "",
    });


    useEffect(() => {

        const loadData = async () => {

            try {

                setLoading(true);
                setError("");

                const [expense, categoriesData] =
                    await Promise.all([
                        getExpenseById(id),
                        getCategories(),
                    ]);

                setCategories(categoriesData);

                setFormData({
                    categoryId:
                        expense.categoryId ||
                        expense.category?.id ||
                        "",

                    amount: expense.amount ?? "",

                    paymentMode:
                        expense.paymentMode || "",

                    expenseDate:
                        expense.expenseDate || "",

                    title:
                        expense.title || "",

                    description:
                        expense.description || "",
                });

            } catch (error) {

                console.error(error);

                setError(
                    "Failed to load expense."
                );

            } finally {

                setLoading(false);
            }
        };

        loadData();

    }, [id]);


    const handleChange = (event) => {

        setFormData({
            ...formData,
            [event.target.name]: event.target.value,
        });
    };


    const handleSubmit = async (event) => {

        event.preventDefault();

        try {

            setSaving(true);
            setError("");

            await updateExpense(id, {
                categoryId:
                    Number(formData.categoryId),

                amount:
                    Number(formData.amount),

                paymentMode:
                    formData.paymentMode,

                expenseDate:
                    formData.expenseDate,

                title:
                    formData.title,

                description:
                    formData.description,
            });

            navigate("/expenses");

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to update expense."
            );

        } finally {

            setSaving(false);
        }
    };


    if (loading) {

        return (
            <Box sx={{ padding: 4 }}>
                <Typography>
                    Loading expense...
                </Typography>
            </Box>
        );
    }


    return (
        <Box
            sx={{
                maxWidth: 700,
                width: "100%",
                margin: { xs: "16px auto", sm: "40px auto" },
                padding: { xs: 0, sm: 2 },
            }}
        >

            <Card>

                <CardContent sx={{ p: { xs: 2, sm: 4 } }}>

                    <Typography
                        variant="h4"
                        gutterBottom
                    >
                        Edit Expense
                    </Typography>

                    <Typography
                        color="text.secondary"
                        sx={{ mb: 3 }}
                    >
                        Update your expense details
                    </Typography>


                    {error && (
                        <Alert
                            severity="error"
                            sx={{ mb: 3 }}
                        >
                            {error}
                        </Alert>
                    )}


                    <Box
                        component="form"
                        onSubmit={handleSubmit}
                    >

                        <FormControl
                            fullWidth
                            margin="normal"
                            required
                        >

                            <InputLabel>
                                Category
                            </InputLabel>

                            <Select
                                name="categoryId"
                                value={formData.categoryId}
                                label="Category"
                                onChange={handleChange}
                            >

                                {categories
                                    .filter(
                                        (category) =>
                                            category.type ===
                                            "EXPENSE"
                                    )
                                    .map((category) => (

                                        <MenuItem
                                            key={category.id}
                                            value={category.id}
                                        >
                                            {category.name}
                                        </MenuItem>

                                    ))}

                            </Select>

                        </FormControl>


                        <TextField
                            fullWidth
                            required
                            margin="normal"
                            label="Amount"
                            name="amount"
                            type="number"
                            value={formData.amount}
                            onChange={handleChange}
                            inputProps={{
                                min: 0.01,
                                step: 0.01,
                            }}
                        />


                        <FormControl
                            fullWidth
                            margin="normal"
                            required
                        >

                            <InputLabel>
                                Payment Mode
                            </InputLabel>

                            <Select
                                name="paymentMode"
                                value={formData.paymentMode}
                                label="Payment Mode"
                                onChange={handleChange}
                            >

                                <MenuItem value="CASH">
                                    Cash
                                </MenuItem>

                                <MenuItem value="CREDIT_CARD">
                                    Credit Card
                                </MenuItem>

                                <MenuItem value="DEBIT_CARD">
                                    Debit Card
                                </MenuItem>

                                <MenuItem value="UPI">
                                    UPI
                                </MenuItem>

                                <MenuItem value="BANK_TRANSFER">
                                    Bank Transfer
                                </MenuItem>

                            </Select>

                        </FormControl>


                        <TextField
                            fullWidth
                            required
                            margin="normal"
                            label="Expense Date"
                            name="expenseDate"
                            type="date"
                            value={formData.expenseDate}
                            onChange={handleChange}
                            slotProps={{
                                inputLabel: {
                                    shrink: true,
                                },
                            }}
                        />


                        <TextField
                            fullWidth
                            required
                            margin="normal"
                            label="Title"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                        />


                        <TextField
                            fullWidth
                            margin="normal"
                            label="Description"
                            name="description"
                            multiline
                            rows={4}
                            value={formData.description}
                            onChange={handleChange}
                        />


                        <Box
                            sx={{
                                display: "flex",
                                justifyContent: "flex-end",
                                gap: 2,
                                mt: 3,
                            }}
                        >

                            <Button
                                variant="outlined"
                                onClick={() =>
                                    navigate("/expenses")
                                }
                            >
                                Cancel
                            </Button>

                            <Button
                                type="submit"
                                variant="contained"
                                disabled={saving}
                            >
                                {saving
                                    ? "Updating..."
                                    : "Update Expense"}
                            </Button>

                        </Box>

                    </Box>

                </CardContent>

            </Card>

        </Box>
    );
}

export default EditExpense;
