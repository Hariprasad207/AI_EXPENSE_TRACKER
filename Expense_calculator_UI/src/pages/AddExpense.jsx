import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

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

import {
    DatePicker,
    LocalizationProvider,
} from "@mui/x-date-pickers";

import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";

import dayjs from "dayjs";

import { createExpense } from "../api/expense";

import { getCategories } from "../api/category";


function AddExpense() {

    const navigate = useNavigate();


    // =========================================================
    // CATEGORIES
    // =========================================================

    const [categories, setCategories] = useState([]);


    // =========================================================
    // FORM DATA
    // =========================================================

    const [formData, setFormData] = useState({

        categoryId: "",

        amount: "",

        paymentMode: "",

        // Default to today's date
        expenseDate: dayjs(),

        title: "",

        description: "",
    });


    // =========================================================
    // ERROR / LOADING
    // =========================================================

    const [error, setError] = useState("");

    const [loading, setLoading] = useState(false);


    // =========================================================
    // LOAD CATEGORIES
    // =========================================================

    useEffect(() => {

        const loadCategories = async () => {

            try {

                const data = await getCategories();

                const categoryList =
                    Array.isArray(data)
                        ? data
                        : data.content || [];

                setCategories(categoryList);

            } catch (error) {

                console.error(
                    "Failed to load categories:",
                    error
                );

                setError(
                    "Failed to load categories."
                );

            }

        };

        loadCategories();

    }, []);


    // =========================================================
    // HANDLE INPUT CHANGE
    // =========================================================

    const handleChange = (event) => {

        setFormData({
            ...formData,
            [event.target.name]:
                event.target.value,
        });

    };


    // =========================================================
    // HANDLE DATE CHANGE
    // =========================================================

    const handleDateChange = (newDate) => {

        setFormData({
            ...formData,
            expenseDate: newDate,
        });

    };


    // =========================================================
    // SUBMIT
    // =========================================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");


        // =====================================================
        // VALIDATE DATE
        // =====================================================

        if (
            !formData.expenseDate ||
            !dayjs(formData.expenseDate).isValid()
        ) {

            setError(
                "Please select a valid expense date."
            );

            return;

        }


        // =====================================================
        // VALIDATE AMOUNT
        // =====================================================

        if (
            !formData.amount ||
            Number(formData.amount) <= 0
        ) {

            setError(
                "Please enter a valid amount."
            );

            return;

        }


        try {

            setLoading(true);


            // =================================================
            // PREPARE REQUEST
            // =================================================

            const expenseData = {

                categoryId:
                    Number(formData.categoryId),

                amount:
                    Number(formData.amount),

                paymentMode:
                    formData.paymentMode,

                expenseDate:
                    formData.expenseDate.format(
                        "YYYY-MM-DD"
                    ),

                title:
                    formData.title,

                description:
                    formData.description,
            };


            console.log(
                "Creating expense:",
                expenseData
            );


            // =================================================
            // API CALL
            // =================================================

            await createExpense(
                expenseData
            );


            // =================================================
            // SUCCESS
            // =================================================

            navigate("/expenses");


        } catch (error) {

            console.error(
                "Failed to create expense:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Failed to create expense."
            );

        } finally {

            setLoading(false);

        }

    };


    // =========================================================
    // UI
    // =========================================================

    return (

        <LocalizationProvider
            dateAdapter={AdapterDayjs}
        >

            <Box
                sx={{
                    maxWidth: 700,
                    width: "100%",
                    margin: { xs: "16px auto", sm: "40px auto" },
                    padding: { xs: 0, sm: 2 },
                }}
            >

                <Card>

                    <CardContent
                        sx={{
                            p: { xs: 2, sm: 4 },
                        }}
                    >

                        {/* ===================================== */}
                        {/* TITLE */}
                        {/* ===================================== */}

                        <Typography
                            variant="h4"
                            gutterBottom
                        >
                            Add Expense
                        </Typography>


                        <Typography
                            color="text.secondary"
                            sx={{ mb: 3 }}
                        >
                            Record a new expense
                        </Typography>


                        {/* ===================================== */}
                        {/* ERROR */}
                        {/* ===================================== */}

                        {error && (

                            <Alert
                                severity="error"
                                sx={{ mb: 3 }}
                            >
                                {error}
                            </Alert>

                        )}


                        {/* ===================================== */}
                        {/* FORM */}
                        {/* ===================================== */}

                        <Box
                            component="form"
                            onSubmit={handleSubmit}
                        >


                            {/* ================================= */}
                            {/* CATEGORY */}
                            {/* ================================= */}

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
                                    value={
                                        formData.categoryId
                                    }
                                    label="Category"
                                    onChange={
                                        handleChange
                                    }
                                >

                                    <MenuItem value="">
                                        Select Category
                                    </MenuItem>


                                    {categories
                                        .filter(
                                            (category) =>
                                                category.type ===
                                                "EXPENSE"
                                        )
                                        .map(
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

                            </FormControl>


                            {/* ================================= */}
                            {/* AMOUNT */}
                            {/* ================================= */}

                            <TextField
                                fullWidth
                                required
                                margin="normal"
                                label="Amount"
                                name="amount"
                                type="number"
                                value={
                                    formData.amount
                                }
                                onChange={
                                    handleChange
                                }
                                inputProps={{
                                    min: 0.01,
                                    step: 0.01,
                                }}
                            />


                            {/* ================================= */}
                            {/* PAYMENT MODE */}
                            {/* ================================= */}

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
                                    value={
                                        formData.paymentMode
                                    }
                                    label="Payment Mode"
                                    onChange={
                                        handleChange
                                    }
                                >

                                    <MenuItem value="">
                                        Select Payment Mode
                                    </MenuItem>

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


                            {/* ================================= */}
                            {/* EXPENSE DATE */}
                            {/* ================================= */}

                            <DatePicker
                                label="Expense Date"

                                value={
                                    formData.expenseDate
                                }

                                onChange={
                                    handleDateChange
                                }

                                disableFuture

                                format="DD/MM/YYYY"

                                slotProps={{
                                    textField: {
                                        fullWidth: true,
                                        required: true,
                                        margin: "normal",
                                        helperText:
                                            "Select the date when the expense occurred.",
                                    },
                                }}
                            />


                            {/* ================================= */}
                            {/* TITLE */}
                            {/* ================================= */}

                            <TextField
                                fullWidth
                                required
                                margin="normal"
                                label="Title"
                                name="title"
                                value={
                                    formData.title
                                }
                                onChange={
                                    handleChange
                                }
                            />


                            {/* ================================= */}
                            {/* DESCRIPTION */}
                            {/* ================================= */}

                            <TextField
                                fullWidth
                                margin="normal"
                                label="Description"
                                name="description"
                                multiline
                                rows={4}
                                value={
                                    formData.description
                                }
                                onChange={
                                    handleChange
                                }
                            />


                            {/* ================================= */}
                            {/* BUTTONS */}
                            {/* ================================= */}

                            <Box
                                sx={{
                                    display: "flex",
                                    gap: 2,
                                    justifyContent:
                                        "flex-end",
                                    mt: 3,
                                    flexWrap: "wrap",
                                }}
                            >

                                <Button
                                    variant="outlined"
                                    disabled={loading}
                                    onClick={() =>
                                        navigate(
                                            "/expenses"
                                        )
                                    }
                                >
                                    Cancel
                                </Button>


                                <Button
                                    type="submit"
                                    variant="contained"
                                    disabled={
                                        loading
                                    }
                                >
                                    {loading
                                        ? "Saving..."
                                        : "Save Expense"}
                                </Button>

                            </Box>

                        </Box>

                    </CardContent>

                </Card>

            </Box>

        </LocalizationProvider>

    );
}


export default AddExpense;
