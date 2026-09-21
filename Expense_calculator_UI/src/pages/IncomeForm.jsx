import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    TextField,
    Typography,
} from "@mui/material";

import {
    createIncome,
    getIncomeById,
    updateIncome,
} from "../api/income";

import { getCategories } from "../api/category";

import {
    DatePicker,
} from "@mui/x-date-pickers/DatePicker";

import {
    LocalizationProvider,
} from "@mui/x-date-pickers/LocalizationProvider";

import {
    AdapterDayjs,
} from "@mui/x-date-pickers/AdapterDayjs";

import dayjs from "dayjs";


function IncomeForm() {

    const navigate = useNavigate();

    const { id } = useParams();

    const isEditMode = Boolean(id);


    // =========================================================
    // FORM STATE
    // =========================================================

    const [formData, setFormData] = useState({
        categoryId: "",
        amount: "",
        incomeDate: dayjs(),
        title: "",
        source: "",
        description: "",
    });


    // =========================================================
    // CATEGORIES
    // =========================================================

    const [categories, setCategories] = useState([]);


    // =========================================================
    // UI STATE
    // =========================================================

    const [loading, setLoading] = useState(false);

    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");


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

                setCategories(
                    categoryList.filter(
                        (category) =>
                            category.type === "INCOME"
                    )
                );

            } catch (error) {

                console.error(error);

                setError(
                    "Failed to load income categories."
                );
            }
        };

        loadCategories();

    }, []);


    // =========================================================
    // LOAD EXISTING INCOME
    // =========================================================

    useEffect(() => {

        if (!isEditMode) {
            return;
        }

        const loadIncome = async () => {

            try {

                setLoading(true);

                setError("");

                const data =
                    await getIncomeById(id);


                setFormData({
                    categoryId:
                        data.categoryId || "",

                    amount:
                        data.amount?.toString() || "",

                    incomeDate:
                        data.incomeDate
                            ? dayjs(data.incomeDate)
                            : dayjs(),

                    title:
                        data.title || "",

                    source:
                        data.source || "",

                    description:
                        data.description || "",
                });

            } catch (error) {

                console.error(error);

                setError(
                    "Failed to load income."
                );

            } finally {

                setLoading(false);

            }
        };

        loadIncome();

    }, [id, isEditMode]);


    // =========================================================
    // INPUT CHANGE
    // =========================================================

    const handleChange = (event) => {

        const {
            name,
            value,
        } = event.target;

        setFormData({
            ...formData,
            [name]: value,
        });

    };


    // =========================================================
    // SUBMIT
    // =========================================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");


        // Basic validation

        if (!formData.categoryId) {

            setError(
                "Please select an income category."
            );

            return;
        }


        if (
            !formData.amount ||
            Number(formData.amount) <= 0
        ) {

            setError(
                "Amount must be greater than 0."
            );

            return;
        }


        if (!formData.title.trim()) {

            setError(
                "Title is required."
            );

            return;
        }


        if (!formData.incomeDate) {

            setError(
                "Income date is required."
            );

            return;
        }


        try {

            setSaving(true);


            const requestData = {

                categoryId:
                    Number(
                        formData.categoryId
                    ),

                amount:
                    Number(
                        formData.amount
                    ),

                incomeDate:
                    formData.incomeDate.format(
                        "YYYY-MM-DD"
                    ),

                title:
                    formData.title.trim(),

                source:
                    formData.source.trim() ||
                    null,

                description:
                    formData.description.trim() ||
                    null,
            };


            if (isEditMode) {

                await updateIncome(
                    id,
                    requestData
                );

            } else {

                await createIncome(
                    requestData
                );

            }


            // Return to income list

            navigate("/income");

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to save income."
            );

        } finally {

            setSaving(false);

        }
    };


    // =========================================================
    // LOADING EXISTING INCOME
    // =========================================================

    if (loading) {

        return (

            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    p: 5,
                }}
            >

                <CircularProgress />

            </Box>

        );
    }


    // =========================================================
    // FORM
    // =========================================================

    return (

        <LocalizationProvider
            dateAdapter={AdapterDayjs}
        >

            <Box
                sx={{
                    maxWidth: 700,
                    mx: "auto",
                    p: 4,
                }}
            >


                {/* ================================================= */}
                {/* HEADER */}
                {/* ================================================= */}

                <Box sx={{ mb: 3 }}>

                    <Typography variant="h4">

                        {isEditMode
                            ? "Edit Income"
                            : "Add Income"}

                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                    >

                        {isEditMode
                            ? "Update your income details"
                            : "Add a new income record"}

                    </Typography>

                </Box>


                {/* ================================================= */}
                {/* ERROR */}
                {/* ================================================= */}

                {error && (

                    <Alert
                        severity="error"
                        sx={{ mb: 2 }}
                    >
                        {error}
                    </Alert>

                )}


                {/* ================================================= */}
                {/* FORM CARD */}
                {/* ================================================= */}

                <Paper
                    sx={{
                        p: 3,
                    }}
                >

                    <Box
                        component="form"
                        onSubmit={handleSubmit}
                    >


                        {/* ========================================= */}
                        {/* TITLE */}
                        {/* ========================================= */}

                        <TextField
                            fullWidth
                            label="Title"
                            name="title"
                            value={
                                formData.title
                            }
                            onChange={
                                handleChange
                            }
                            margin="normal"
                            required
                        />


                        {/* ========================================= */}
                        {/* AMOUNT */}
                        {/* ========================================= */}

                        <TextField
                            fullWidth
                            label="Amount"
                            name="amount"
                            type="number"
                            value={
                                formData.amount
                            }
                            onChange={
                                handleChange
                            }
                            margin="normal"
                            required
                            inputProps={{
                                min: 0.01,
                                step: 0.01,
                            }}
                        />


                        {/* ========================================= */}
                        {/* CATEGORY */}
                        {/* ========================================= */}

                        <FormControl
                            fullWidth
                            margin="normal"
                            required
                        >

                            <InputLabel>
                                Income Category
                            </InputLabel>

                            <Select
                                name="categoryId"
                                value={
                                    formData.categoryId
                                }
                                label="Income Category"
                                onChange={
                                    handleChange
                                }
                            >

                                <MenuItem value="">
                                    Select Category
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

                        </FormControl>


                        {/* ========================================= */}
                        {/* INCOME DATE */}
                        {/* ========================================= */}

                        <DatePicker
                            label="Income Date"
                            value={
                                formData.incomeDate
                            }
                            onChange={(newValue) =>
                                setFormData({
                                    ...formData,
                                    incomeDate:
                                        newValue,
                                })
                            }
                            slotProps={{
                                textField: {
                                    fullWidth: true,
                                    margin: "normal",
                                    required: true,
                                },
                            }}
                        />


                        {/* ========================================= */}
                        {/* SOURCE */}
                        {/* ========================================= */}

                        <TextField
                            fullWidth
                            label="Source"
                            name="source"
                            value={
                                formData.source
                            }
                            onChange={
                                handleChange
                            }
                            margin="normal"
                            placeholder="Example: Company, Freelance"
                        />


                        {/* ========================================= */}
                        {/* DESCRIPTION */}
                        {/* ========================================= */}

                        <TextField
                            fullWidth
                            multiline
                            rows={4}
                            label="Description"
                            name="description"
                            value={
                                formData.description
                            }
                            onChange={
                                handleChange
                            }
                            margin="normal"
                        />


                        {/* ========================================= */}
                        {/* BUTTONS */}
                        {/* ========================================= */}

                        <Box
                            sx={{
                                display: "flex",
                                justifyContent:
                                    "flex-end",
                                gap: 2,
                                mt: 3,
                            }}
                        >

                            <Button
                                variant="outlined"
                                onClick={() =>
                                    navigate(
                                        "/income"
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
                                        ? "Update Income"
                                        : "Add Income"}

                            </Button>

                        </Box>

                    </Box>

                </Paper>

            </Box>

        </LocalizationProvider>
    );
}


export default IncomeForm;