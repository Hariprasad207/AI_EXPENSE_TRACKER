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

import {
    getIncome,
    deleteIncome,
} from "../api/income";

import {
    getCategories,
} from "../api/category";


function IncomeList() {

    const navigate = useNavigate();


    // =========================================================
    // INCOME DATA
    // =========================================================

    const [income, setIncome] = useState([]);

    const [page, setPage] = useState(0);

    const [rowsPerPage, setRowsPerPage] =
        useState(10);

    const [totalElements, setTotalElements] =
        useState(0);


    // =========================================================
    // UI STATE
    // =========================================================

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");


    // =========================================================
    // CATEGORIES
    // =========================================================

    const [categories, setCategories] =
        useState([]);


    // =========================================================
    // FILTER FORM
    // =========================================================
    // What the user is currently selecting.
    // These values are not sent until Apply Filters.
    // =========================================================

    const [filterForm, setFilterForm] =
        useState({
            categoryId: "",
            fromDate: null,
            toDate: null,
        });


    // =========================================================
    // APPLIED FILTERS
    // =========================================================
    // These are the filters actually sent to backend.
    // =========================================================

    const [appliedFilters, setAppliedFilters] =
        useState({});


    // =========================================================
    // LOAD CATEGORIES
    // =========================================================

    useEffect(() => {

        const loadCategories = async () => {

            try {

                const data =
                    await getCategories();

                const categoryList =
                    Array.isArray(data)
                        ? data
                        : data.content || [];

                setCategories(
                    categoryList.filter(
                        (category) =>
                            category.type ===
                            "INCOME"
                    )
                );

            } catch (error) {

                console.error(
                    "Failed to load categories:",
                    error
                );

            }

        };

        loadCategories();

    }, []);


    // =========================================================
    // LOAD INCOME
    // =========================================================

    const loadIncome = async () => {

        try {

            setLoading(true);

            setError("");


            const data = await getIncome({

                page,

                size: rowsPerPage,

                ...appliedFilters,

            });


            setIncome(
                data.content || []
            );


            setTotalElements(
                data.totalElements || 0
            );


        } catch (error) {

            console.error(error);

            setError(
                "Failed to load income."
            );

        } finally {

            setLoading(false);

        }

    };


    // =========================================================
    // LOAD WHEN PAGE / FILTER CHANGES
    // =========================================================

    useEffect(() => {

        loadIncome();

    }, [
        page,
        rowsPerPage,
        appliedFilters,
    ]);


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

        setError("");

    };


    // =========================================================
    // CLEAR FILTERS
    // =========================================================

    const handleClearFilters = () => {

        setFilterForm({

            categoryId: "",

            fromDate: null,

            toDate: null,

        });

        setAppliedFilters({});

        setPage(0);

        setError("");

    };


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
    // ROWS PER PAGE
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
    // DELETE
    // =========================================================

    const handleDelete = async (id) => {

        const confirmed =
            window.confirm(
                "Are you sure you want to delete this income?"
            );


        if (!confirmed) {

            return;

        }


        try {

            setError("");

            await deleteIncome(id);

            await loadIncome();

        } catch (error) {

            console.error(error);

            setError(
                "Failed to delete income."
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

            <Box sx={{ p: { xs: 0, sm: 1, md: 4 } }}>


                {/* ================================================= */}
                {/* HEADER */}
                {/* ================================================= */}

                <Box
                    sx={{
                        display: "flex",
                        justifyContent:
                            "space-between",
                        alignItems: "center",
                        mb: 3,
                        gap: 2,
                        flexWrap: "wrap",
                    }}
                >

                    <Box>

                        <Typography variant="h4">
                            Income
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            Manage your income
                        </Typography>

                    </Box>


                    <Button
                        variant="contained"
                        onClick={() =>
                            navigate(
                                "/income/new"
                            )
                        }
                    >
                        + Add Income
                    </Button>

                </Box>


                {/* ================================================= */}
                {/* FILTERS */}
                {/* ================================================= */}

                <Paper
                    sx={{
                        p: { xs: 2, sm: 3 },
                        mb: 3,
                    }}
                >

                    <Typography
                        variant="h6"
                        sx={{ mb: 2 }}
                    >
                        Filter Income
                    </Typography>


                    <Box
                        sx={{
                            display: "grid",
                            gridTemplateColumns: {
                                xs: "1fr",
                                sm: "1fr 1fr",
                                md: "repeat(3, 1fr)",
                            },
                            gap: 2,
                        }}
                    >


                        {/* CATEGORY */}

                        <FormControl fullWidth>

                            <InputLabel>
                                Category
                            </InputLabel>

                            <Select
                                value={
                                    filterForm.categoryId
                                }
                                label="Category"
                                onChange={
                                    (event) =>
                                        setFilterForm({
                                            ...filterForm,
                                            categoryId:
                                                event
                                                    .target
                                                    .value,
                                        })
                                }
                            >

                                <MenuItem value="">
                                    All Categories
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


                        {/* FROM DATE */}

                        <DatePicker
                            label="From Date"
                            value={
                                filterForm.fromDate
                            }
                            onChange={
                                (newValue) =>
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


                        {/* TO DATE */}

                        <DatePicker
                            label="To Date"
                            value={
                                filterForm.toDate
                            }
                            onChange={
                                (newValue) =>
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


                    {/* FILTER BUTTONS */}

                    <Box
                        sx={{
                            display: "flex",
                            justifyContent:
                                "flex-end",
                            gap: 2,
                            mt: 3,
                            flexWrap: "wrap",
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
                            justifyContent:
                                "center",
                            p: 5,
                        }}
                    >

                        <CircularProgress />

                    </Box>

                ) : (

                    <Paper>

                        <TableContainer>

                            <Table sx={{ minWidth: 680 }}>

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
                                            Source
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


                                <TableBody>

                                    {income.map(
                                        (item) => (

                                            <TableRow
                                                key={
                                                    item.id
                                                }
                                                hover
                                            >

                                                <TableCell>
                                                    {
                                                        item.incomeDate
                                                    }
                                                </TableCell>


                                                <TableCell>
                                                    {
                                                        item.title
                                                    }
                                                </TableCell>


                                                <TableCell>
                                                    {
                                                        item.categoryName
                                                    }
                                                </TableCell>


                                                <TableCell>
                                                    {
                                                        item.source ||
                                                        "-"
                                                    }
                                                </TableCell>


                                                <TableCell align="right">
                                                    ₹
                                                    {
                                                        item.amount
                                                    }
                                                </TableCell>


                                                {/* EDIT */}

                                                <TableCell align="center">

                                                    <Button
                                                        variant="outlined"
                                                        size="small"
                                                        onClick={() =>
                                                            navigate(
                                                                `/income/${item.id}/edit`
                                                            )
                                                        }
                                                    >
                                                        Edit
                                                    </Button>

                                                </TableCell>


                                                {/* DELETE */}

                                                <TableCell align="center">

                                                    <Button
                                                        variant="outlined"
                                                        color="error"
                                                        size="small"
                                                        onClick={() =>
                                                            handleDelete(
                                                                item.id
                                                            )
                                                        }
                                                    >
                                                        Delete
                                                    </Button>

                                                </TableCell>

                                            </TableRow>

                                        )
                                    )}


                                    {/* EMPTY */}

                                    {income.length === 0 && (

                                        <TableRow>

                                            <TableCell
                                                colSpan={7}
                                                align="center"
                                            >
                                                No income found.
                                            </TableCell>

                                        </TableRow>

                                    )}

                                </TableBody>

                            </Table>

                        </TableContainer>


                        {/* ================================================= */}
                        {/* PAGINATION */}
                        {/* ================================================= */}

                        <TablePagination

                            component="div"

                            count={
                                totalElements
                            }

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


export default IncomeList;
