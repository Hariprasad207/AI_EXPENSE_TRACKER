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
    TextField,
    Typography,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
} from "@mui/material";

import {
    Save,
    ArrowBack,
} from "@mui/icons-material";

import {
    useNavigate,
    useParams,
} from "react-router-dom";

import {
    createCategory,
    getCategoryById,
    updateCategory,
} from "../api/category";


function CategoryForm() {

    const navigate = useNavigate();

    const { id } = useParams();


    const isEditMode = Boolean(id);


    const [formData, setFormData] =
        useState({
            name: "",
            type: "EXPENSE",
            icon: "",
        });


    const [loading, setLoading] =
        useState(isEditMode);


    const [submitting, setSubmitting] =
        useState(false);


    const [error, setError] =
        useState("");


    /* =========================
        LOAD CATEGORY FOR EDIT
    ========================= */

    useEffect(() => {

        if (!isEditMode) {
            return;
        }


        const loadCategory = async () => {

            try {

                setLoading(true);

                const data =
                    await getCategoryById(id);


                setFormData({
                    name: data.name || "",
                    type: data.type || "EXPENSE",
                    icon: data.icon || "",
                });

            } catch (error) {

                setError(
                    error.response?.data?.message ||
                    "Failed to load category"
                );

            } finally {

                setLoading(false);

            }

        };


        loadCategory();

    }, [id, isEditMode]);


    /* =========================
        HANDLE INPUT CHANGE
    ========================= */

    const handleChange = (event) => {

        const {
            name,
            value,
        } = event.target;


        setFormData((previousData) => ({
            ...previousData,
            [name]: value,
        }));

    };


    /* =========================
        HANDLE SUBMIT
    ========================= */

    const handleSubmit = async (event) => {

        event.preventDefault();


        if (!formData.name.trim()) {

            setError(
                "Category name is required"
            );

            return;

        }


        try {

            setSubmitting(true);

            setError("");


            const payload = {

                name:
                    formData.name.trim(),

                type:
                    formData.type,

                icon:
                    formData.icon.trim(),

            };


            if (isEditMode) {

                await updateCategory(
                    id,
                    payload
                );

            } else {

                await createCategory(
                    payload
                );

            }


            navigate("/categories");


        } catch (error) {

            setError(
                error.response?.data?.message ||
                "Failed to save category"
            );

        } finally {

            setSubmitting(false);

        }

    };


    /* =========================
        LOADING STATE
    ========================= */

    if (loading) {

        return (

            <Box
                sx={{
                    display: "flex",

                    justifyContent:
                        "center",

                    alignItems:
                        "center",

                    minHeight:
                        "70vh",
                }}
            >

                <CircularProgress />

            </Box>

        );

    }


    return (

        <Box
            sx={{
                p: { xs: 0, sm: 2, md: 3 },

                maxWidth:
                    650,

                mx:
                    "auto",
            }}
        >


            {/* =========================
                HEADER
            ========================= */}

            <Box
                sx={{
                    display:
                        "flex",

                    alignItems:
                        "center",

                    gap:
                        2,

                    mb:
                        3,
                    flexWrap: "wrap",
                }}
            >

                <Button

                    startIcon={
                        <ArrowBack />
                    }

                    onClick={() =>
                        navigate("/categories")
                    }

                >
                    Back
                </Button>


                <Typography
                    variant="h4"
                    fontWeight="bold"
                >

                    {isEditMode
                        ? "Edit Category"
                        : "Add Category"}

                </Typography>

            </Box>


            {/* =========================
                FORM CARD
            ========================= */}

            <Card>

                <CardContent
                    sx={{
                        p: 3,
                    }}
                >


                    {/* ERROR */}

                    {error && (

                        <Alert
                            severity="error"

                            sx={{
                                mb:
                                    3,
                            }}
                        >

                            {error}

                        </Alert>

                    )}


                    <Box
                        component="form"

                        onSubmit={
                            handleSubmit
                        }
                    >


                        {/* =========================
                            CATEGORY NAME
                        ========================= */}

                        <TextField

                            fullWidth

                            required

                            label="Category Name"

                            name="name"

                            value={
                                formData.name
                            }

                            onChange={
                                handleChange
                            }

                            placeholder={
                                formData.type ===
                                    "EXPENSE"
                                    ? "Example: Food"
                                    : "Example: Salary"
                            }

                            inputProps={{
                                maxLength:
                                    50,
                            }}

                            sx={{
                                mb:
                                    3,
                            }}

                        />


                        {/* =========================
                            CATEGORY TYPE
                        ========================= */}

                        <FormControl

                            fullWidth

                            sx={{
                                mb:
                                    3,
                            }}

                        >

                            <InputLabel>
                                Category Type
                            </InputLabel>


                            <Select

                                name="type"

                                value={
                                    formData.type
                                }

                                label="Category Type"

                                onChange={
                                    handleChange
                                }

                            >

                                <MenuItem
                                    value="EXPENSE"
                                >
                                    Expense
                                </MenuItem>


                                <MenuItem
                                    value="INCOME"
                                >
                                    Income
                                </MenuItem>

                            </Select>

                        </FormControl>


                        {/* =========================
                            ICON
                        ========================= */}

                        <TextField

                            fullWidth

                            label="Icon"

                            name="icon"

                            value={
                                formData.icon
                            }

                            onChange={
                                handleChange
                            }

                            placeholder="Example: 🍔"

                            helperText={
                                "Optional. You can use an emoji."
                            }

                            inputProps={{
                                maxLength:
                                    50,
                            }}

                            sx={{
                                mb:
                                    3,
                            }}

                        />


                        {/* =========================
                            ACTION BUTTONS
                        ========================= */}

                        <Box

                            sx={{
                                display:
                                    "flex",

                                justifyContent:
                                    "flex-end",

                                gap:
                                    2,
                            }}

                        >


                            <Button

                                variant="outlined"

                                disabled={
                                    submitting
                                }

                                onClick={() =>
                                    navigate(
                                        "/categories"
                                    )
                                }

                            >

                                Cancel

                            </Button>


                            <Button

                                type="submit"

                                variant="contained"

                                startIcon={
                                    <Save />
                                }

                                disabled={
                                    submitting
                                }

                            >

                                {submitting
                                    ? "Saving..."
                                    : isEditMode
                                        ? "Update Category"
                                        : "Create Category"}

                            </Button>


                        </Box>


                    </Box>

                </CardContent>

            </Card>

        </Box>

    );

}


export default CategoryForm;
