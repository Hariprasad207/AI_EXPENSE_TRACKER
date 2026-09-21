import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

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
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    Typography,
} from "@mui/material";

import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";

import dayjs from "dayjs";

import {
    getExpenses,
    deleteExpense,
} from "../api/expense";

import { getCategories } from "../api/category";


function ExpenseList() {

    const navigate = useNavigate();


    // =========================================================
    // EXPENSE DATA
    // =========================================================

    const [expenses, setExpenses] = useState([]);

    const [page, setPage] = useState(0);

    const [rowsPerPage, setRowsPerPage] = useState(10);

    const [totalElements, setTotalElements] = useState(0);

    const [loading, setLoading] = useState(false);

    const [error, setError] = useState("");

    const [categories, setCategories] = useState([]);


    const [filterForm, setFilterForm] = useState({
        categoryId: "",
        paymentMode: "",
        fromDate: null,
        toDate: null,
    });

    const [appliedFilters, setAppliedFilters] = useState({});


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

            }
        };

        loadCategories();

    }, []);


    const loadExpenses = async () => {

        try {

            setLoading(true);

            setError("");

            const data = await getExpenses({
                page,
                size: rowsPerPage,
                ...appliedFilters,
            });

            setExpenses(data.content || []);

            setTotalElements(
                data.totalElements || 0
            );

        } catch (error) {

            console.error(error);

            setError(
                "Failed to load expenses."
            );

        } finally {

            setLoading(false);

        }
    };

    useEffect(() => {

        loadExpenses();

    }, [
        page,
        rowsPerPage,
        appliedFilters,
    ]);


    // =========================================================
    // PAGE CHANGE
    // =========================================================

    const handlePageChange = (
        event,
        newPage
    ) => {

        setPage(newPage);

    };


    // =========================================================
    // ROWS PER PAGE CHANGE
    // =========================================================

    const handleRowsPerPageChange = (
        event
    ) => {

        setRowsPerPage(
            parseInt(
                event.target.value,
                10
            )
        );

        setPage(0);

    };


    // =========================================================
    // APPLY FILTERS
    // =========================================================

    const handleApplyFilters = () => {

        // Validate date range

        if (
            filterForm.fromDate &&
            filterForm.toDate &&
            filterForm.fromDate.isAfter(
                filterForm.toDate,
                "day"
            )
        ) {

            setError(
                "From date cannot be after To date."
            );

            return;
        }


        const filters = {};


        // Category

        if (filterForm.categoryId) {

            filters.categoryId =
                filterForm.categoryId;

        }


        // Payment mode

        if (filterForm.paymentMode) {

            filters.paymentMode =
                filterForm.paymentMode;

        }


        // From date

        if (filterForm.fromDate) {

            filters.fromDate =
                filterForm.fromDate.format(
                    "YYYY-MM-DD"
                );

        }


        // To date

        if (filterForm.toDate) {

            filters.toDate =
                filterForm.toDate.format(
                    "YYYY-MM-DD"
                );

        }


        setPage(0);

        setAppliedFilters(filters);

    };


    // =========================================================
    // CLEAR FILTERS
    // =========================================================

    const handleClearFilters = () => {

        setFilterForm({
            categoryId: "",
            paymentMode: "",
            fromDate: null,
            toDate: null,
        });

        setAppliedFilters({});

        setPage(0);

        setError("");

    };


    // =========================================================
    // DELETE EXPENSE
    // =========================================================

    const handleDelete = async (id) => {

        const confirmed = window.confirm(
            "Are you sure you want to delete this expense?"
        );


        if (!confirmed) {

            return;

        }


        try {

            setError("");

            await deleteExpense(id);

            await loadExpenses();

        } catch (error) {

            console.error(error);

            setError(
                "Failed to delete expense."
            );

        }

    };


    // =========================================================
    // RENDER
    // =========================================================

    return (

        <LocalizationProvider
            dateAdapter={AdapterDayjs}
        >

            <Box sx={{ padding: 4 }}>


                {/* ================================================= */}
                {/* HEADER */}
                {/* ================================================= */}

                <Box
                    sx={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        mb: 3,
                    }}
                >

                    <Box>

                        <Typography variant="h4">
                            Expenses
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            Manage your expenses
                        </Typography>

                    </Box>


                    <Button
                        variant="contained"
                        onClick={() =>
                            navigate(
                                "/expenses/new"
                            )
                        }
                    >
                        + Add Expense
                    </Button>

                </Box>


                {/* ================================================= */}
                {/* FILTER SECTION */}
                {/* ================================================= */}

                <Paper
                    sx={{
                        p: 3,
                        mb: 3,
                    }}
                >

                    <Typography
                        variant="h6"
                        sx={{ mb: 2 }}
                    >
                        Filter Expenses
                    </Typography>


                    <Box
                        sx={{
                            display: "grid",
                            gridTemplateColumns: {
                                xs: "1fr",
                                sm: "1fr 1fr",
                                md: "repeat(4, 1fr)",
                            },
                            gap: 2,
                        }}
                    >


                        {/* ========================================= */}
                        {/* CATEGORY */}
                        {/* ========================================= */}

                        <FormControl fullWidth>

                            <InputLabel>
                                Category
                            </InputLabel>

                            <Select
                                value={
                                    filterForm.categoryId
                                }
                                label="Category"
                                onChange={(event) =>
                                    setFilterForm({
                                        ...filterForm,
                                        categoryId:
                                            event.target.value,
                                    })
                                }
                            >

                                <MenuItem value="">
                                    All Categories
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


                        {/* ========================================= */}
                        {/* PAYMENT MODE */}
                        {/* ========================================= */}

                        <FormControl fullWidth>

                            <InputLabel>
                                Payment Mode
                            </InputLabel>

                            <Select
                                value={
                                    filterForm.paymentMode
                                }
                                label="Payment Mode"
                                onChange={(event) =>
                                    setFilterForm({
                                        ...filterForm,
                                        paymentMode:
                                            event.target.value,
                                    })
                                }
                            >

                                <MenuItem value="">
                                    All Payment Modes
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


                        {/* ========================================= */}
                        {/* FROM DATE */}
                        {/* ========================================= */}

                        <DatePicker
                            label="From Date"
                            value={
                                filterForm.fromDate
                            }
                            onChange={(newValue) =>
                                setFilterForm({
                                    ...filterForm,
                                    fromDate:
                                        newValue,
                                })
                            }
                            slotProps={{
                                textField: {
                                    fullWidth: true,
                                },
                            }}
                        />


                        {/* ========================================= */}
                        {/* TO DATE */}
                        {/* ========================================= */}

                        <DatePicker
                            label="To Date"
                            value={
                                filterForm.toDate
                            }
                            onChange={(newValue) =>
                                setFilterForm({
                                    ...filterForm,
                                    toDate:
                                        newValue,
                                })
                            }
                            slotProps={{
                                textField: {
                                    fullWidth: true,
                                },
                            }}
                        />

                    </Box>


                    {/* ================================================= */}
                    {/* FILTER BUTTONS */}
                    {/* ================================================= */}

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
                            onClick={
                                handleClearFilters
                            }
                        >
                            Clear
                        </Button>


                        <Button
                            variant="contained"
                            onClick={
                                handleApplyFilters
                            }
                        >
                            Apply Filters
                        </Button>

                    </Box>

                </Paper>


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
                {/* TABLE */}
                {/* ================================================= */}

                {loading ? (

                    <Box
                        sx={{
                            display: "flex",
                            justifyContent: "center",
                            padding: 5,
                        }}
                    >

                        <CircularProgress />

                    </Box>

                ) : (

                    <Paper>

                        <TableContainer>

                            <Table>

                                {/* ================================= */}
                                {/* TABLE HEADER */}
                                {/* ================================= */}

                                <TableHead>

                                    <TableRow>

                                        <TableCell>
                                            Date
                                        </TableCell>

                                        <TableCell>
                                            Title
                                        </TableCell>

                                        <TableCell>
                                            Category
                                        </TableCell>

                                        <TableCell>
                                            Payment Mode
                                        </TableCell>

                                        <TableCell align="right">
                                            Amount
                                        </TableCell>

                                        <TableCell align="center">
                                            Edit
                                        </TableCell>

                                        <TableCell align="center">
                                            Delete
                                        </TableCell>

                                    </TableRow>

                                </TableHead>


                                {/* ================================= */}
                                {/* TABLE BODY */}
                                {/* ================================= */}

                                <TableBody>

                                    {expenses.map(
                                        (expense) => (

                                            <TableRow
                                                key={
                                                    expense.id
                                                }
                                                hover
                                            >

                                                {/* Date */}

                                                <TableCell>
                                                    {
                                                        expense.expenseDate
                                                    }
                                                </TableCell>


                                                {/* Title */}

                                                <TableCell>
                                                    {
                                                        expense.title
                                                    }
                                                </TableCell>


                                                {/* Category */}

                                                <TableCell>
                                                    {
                                                        expense.categoryName ||
                                                        expense.category?.name
                                                    }
                                                </TableCell>


                                                {/* Payment */}

                                                <TableCell>
                                                    {
                                                        expense.paymentMode
                                                    }
                                                </TableCell>


                                                {/* Amount */}

                                                <TableCell align="right">
                                                    ₹
                                                    {
                                                        expense.amount
                                                    }
                                                </TableCell>


                                                {/* Edit */}

                                                <TableCell align="center">

                                                    <Button
                                                        variant="outlined"
                                                        size="small"
                                                        onClick={() =>
                                                            navigate(
                                                                `/expenses/${expense.id}/edit`
                                                            )
                                                        }
                                                    >
                                                        Edit
                                                    </Button>

                                                </TableCell>


                                                {/* Delete */}

                                                <TableCell align="center">

                                                    <Button
                                                        variant="outlined"
                                                        color="error"
                                                        size="small"
                                                        onClick={() =>
                                                            handleDelete(
                                                                expense.id
                                                            )
                                                        }
                                                    >
                                                        Delete
                                                    </Button>

                                                </TableCell>

                                            </TableRow>

                                        )
                                    )}


                                    {/* ================================= */}
                                    {/* EMPTY STATE */}
                                    {/* ================================= */}

                                    {expenses.length === 0 && (

                                        <TableRow>

                                            <TableCell
                                                colSpan={7}
                                                align="center"
                                            >
                                                No expenses found.
                                            </TableCell>

                                        </TableRow>

                                    )}

                                </TableBody>

                            </Table>

                        </TableContainer>


                        {/* ================================= */}
                        {/* PAGINATION */}
                        {/* ================================= */}

                        <TablePagination
                            component="div"
                            count={totalElements}
                            page={page}
                            onPageChange={
                                handlePageChange
                            }
                            rowsPerPage={
                                rowsPerPage
                            }
                            onRowsPerPageChange={
                                handleRowsPerPageChange
                            }
                            rowsPerPageOptions={[
                                5,
                                10,
                                25,
                            ]}
                        />

                    </Paper>

                )}

            </Box>

        </LocalizationProvider>

    );
}


export default ExpenseList;