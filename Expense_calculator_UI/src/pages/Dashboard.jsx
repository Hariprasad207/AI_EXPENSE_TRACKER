import { useEffect, useState } from "react";

import {
    Box,
    Card,
    CardContent,
    Typography,
    Grid,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    CircularProgress,
    Alert,
    Divider,
} from "@mui/material";

import {
    PieChart,
    Pie,
    Cell,
    Tooltip,
    Legend,
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
} from "recharts";

import { getDashboardSummary } from "../api/dashboard";


/* =========================================================
   MONTHS
========================================================= */

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


/* =========================================================
   YEARS
========================================================= */

const currentYear = new Date().getFullYear();

const years = Array.from(
    { length: 5 },
    (_, index) => currentYear - index
);

const CHART_COLORS = [
    "#1976d2",
    "#2e7d32",
    "#ed6c02",
    "#9c27b0",
    "#d32f2f",
    "#0288d1",
    "#7b1fa2",
    "#388e3c",
];


/* =========================================================
   DASHBOARD COMPONENT
========================================================= */

function Dashboard() {

    const [month, setMonth] = useState(
        new Date().getMonth() + 1
    );

    const [year, setYear] = useState(currentYear);

    const [dashboard, setDashboard] = useState(null);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");


    /* =====================================================
       LOAD DASHBOARD WHEN MONTH/YEAR CHANGES
    ===================================================== */

    useEffect(() => {
        loadDashboard();
    }, [month, year]);


    /* =====================================================
       LOAD DASHBOARD DATA
    ===================================================== */

    const loadDashboard = async () => {

        try {

            setLoading(true);
            setError("");

            const data = await getDashboardSummary(
                month,
                year
            );

            setDashboard(data);

        } catch (err) {

            console.error(err);

            setError(
                err.response?.data?.message ||
                "Failed to load dashboard data."
            );

        } finally {

            setLoading(false);
        }
    };


    /* =====================================================
       FORMAT CURRENCY
    ===================================================== */

    const formatCurrency = (value) => {

        return new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 2,
        }).format(value || 0);
    };


    /* =====================================================
       LOADING STATE
    ===================================================== */

    if (loading) {

        return (
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    minHeight: "400px",
                }}
            >
                <CircularProgress />
            </Box>
        );
    }


    /* =====================================================
       ERROR STATE
    ===================================================== */

    if (error) {

        return (
            <Box sx={{ p: 3 }}>
                <Alert severity="error">
                    {error}
                </Alert>
            </Box>
        );
    }


    /* =====================================================
       NO DATA
    ===================================================== */

    if (!dashboard) {
        return null;
    }


    /* =====================================================
       DASHBOARD UI
    ===================================================== */

    return (

        <Box sx={{ p: 3 }}>

            {/* =================================================
                PAGE HEADER
            ================================================= */}

            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 4,
                    flexWrap: "wrap",
                    gap: 2,
                }}
            >

                {/* PAGE TITLE */}

                <Box>

                    <Typography
                        variant="h4"
                        fontWeight="bold"
                    >
                        Dashboard
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                    >
                        Overview of your finances
                    </Typography>

                </Box>


                {/* =================================================
                    MONTH / YEAR FILTER
                ================================================= */}

                <Box
                    sx={{
                        display: "flex",
                        gap: 2,
                    }}
                >

                    {/* MONTH */}

                    <FormControl
                        size="small"
                        sx={{ minWidth: 140 }}
                    >

                        <InputLabel>
                            Month
                        </InputLabel>

                        <Select
                            value={month}
                            label="Month"
                            onChange={(e) =>
                                setMonth(
                                    Number(e.target.value)
                                )
                            }
                        >

                            {months.map((item) => (

                                <MenuItem
                                    key={item.value}
                                    value={item.value}
                                >
                                    {item.label}
                                </MenuItem>

                            ))}

                        </Select>

                    </FormControl>


                    {/* YEAR */}

                    <FormControl
                        size="small"
                        sx={{ minWidth: 110 }}
                    >

                        <InputLabel>
                            Year
                        </InputLabel>

                        <Select
                            value={year}
                            label="Year"
                            onChange={(e) =>
                                setYear(
                                    Number(e.target.value)
                                )
                            }
                        >

                            {years.map((item) => (

                                <MenuItem
                                    key={item}
                                    value={item}
                                >
                                    {item}
                                </MenuItem>

                            ))}

                        </Select>

                    </FormControl>

                </Box>

            </Box>


            {/* =================================================
                SUMMARY CARDS
            ================================================= */}

            <Grid
                container
                spacing={3}
                sx={{ mb: 4 }}
            >

                {/* TOTAL INCOME */}

                <Grid
                    size={{
                        xs: 12,
                        sm: 6,
                        md: 3,
                    }}
                >

                    <SummaryCard
                        title="Total Income"
                        value={formatCurrency(
                            dashboard.totalIncome
                        )}
                    />

                </Grid>


                {/* TOTAL EXPENSE */}

                <Grid
                    size={{
                        xs: 12,
                        sm: 6,
                        md: 3,
                    }}
                >

                    <SummaryCard
                        title="Total Expense"
                        value={formatCurrency(
                            dashboard.totalExpense
                        )}
                    />

                </Grid>


                {/* BALANCE */}

                <Grid
                    size={{
                        xs: 12,
                        sm: 6,
                        md: 3,
                    }}
                >

                    <SummaryCard
                        title="Balance"
                        value={formatCurrency(
                            dashboard.balance
                        )}
                    />

                </Grid>


                {/* SAVINGS */}

                <Grid
                    size={{
                        xs: 12,
                        sm: 6,
                        md: 3,
                    }}
                >

                    <SummaryCard
                        title="Savings"
                        value={formatCurrency(
                            dashboard.savings
                        )}
                    />

                </Grid>

            </Grid>


            {/* =================================================
                CHARTS
            ================================================= */}

            <Grid
                container
                spacing={3}
            >

                {/* =================================================
                    EXPENSE BY CATEGORY
                ================================================= */}

                <Grid
                    size={{
                        xs: 12,
                        md: 6,
                    }}
                >

                    <Card
                        sx={{
                            height: "100%",
                        }}
                    >

                        <CardContent>

                            <Typography
                                variant="h6"
                                fontWeight="bold"
                                sx={{ mb: 2 }}
                            >
                                Expense by Category
                            </Typography>

                            <Divider
                                sx={{ mb: 2 }}
                            />


                            {/* NO CATEGORY DATA */}

                            {dashboard.expenseByCategory?.length === 0 ? (

                                <Box
                                    sx={{
                                        height: 300,
                                        display: "flex",
                                        justifyContent: "center",
                                        alignItems: "center",
                                    }}
                                >

                                    <Typography
                                        color="text.secondary"
                                    >
                                        No expense data available
                                    </Typography>

                                </Box>

                            ) : (

                                /* =================================================
                                   PIE CHART
                                ================================================= */

                                <ResponsiveContainer
                                    width="100%"
                                    height={350}
                                >

                                    <PieChart>

                                        <Pie
                                            data={
                                                dashboard.expenseByCategory
                                            }
                                            dataKey="amount"
                                            nameKey="categoryName"
                                            cx="50%"
                                            cy="50%"
                                            outerRadius={120}
                                            label
                                        >
                                            {dashboard.expenseByCategory.map((_, index) => (
                                                <Cell
                                                    key={`cell-${index}`}
                                                    fill={CHART_COLORS[index % CHART_COLORS.length]}
                                                />
                                            ))}
                                        </Pie>


                                        <Tooltip
                                            formatter={(value) =>
                                                formatCurrency(value)
                                            }
                                        />


                                        <Legend />

                                    </PieChart>

                                </ResponsiveContainer>

                            )}

                        </CardContent>

                    </Card>

                </Grid>


                {/* =================================================
                    EXPENSE BY PAYMENT MODE
                ================================================= */}

                <Grid
                    size={{
                        xs: 12,
                        md: 6,
                    }}
                >

                    <Card
                        sx={{
                            height: "100%",
                        }}
                    >

                        <CardContent>

                            <Typography
                                variant="h6"
                                fontWeight="bold"
                                sx={{ mb: 2 }}
                            >
                                Expense by Payment Mode
                            </Typography>

                            <Divider
                                sx={{ mb: 2 }}
                            />


                            {/* NO PAYMENT DATA */}

                            {dashboard.expenseByPaymentMode?.length === 0 ? (

                                <Box
                                    sx={{
                                        height: 300,
                                        display: "flex",
                                        justifyContent: "center",
                                        alignItems: "center",
                                    }}
                                >

                                    <Typography
                                        color="text.secondary"
                                    >
                                        No payment data available
                                    </Typography>

                                </Box>

                            ) : (

                                /* =================================================
                                   BAR CHART
                                ================================================= */

                                <ResponsiveContainer
                                    width="100%"
                                    height={350}
                                >

                                    <BarChart
                                        data={
                                            dashboard.expenseByPaymentMode
                                        }
                                    >

                                        <CartesianGrid />


                                        <XAxis
                                            dataKey="paymentMode"
                                        />


                                        <YAxis />


                                        <Tooltip
                                            formatter={(value) =>
                                                formatCurrency(value)
                                            }
                                        />
                                        <Bar
                                            dataKey="amount"
                                            name="Expense"
                                            fill="#1976d2"
                                        />
                                    </BarChart>

                                </ResponsiveContainer>

                            )}

                        </CardContent>

                    </Card>

                </Grid>

            </Grid>

        </Box>
    );
}


/* =========================================================
   SUMMARY CARD COMPONENT
========================================================= */

function SummaryCard({ title, value }) {

    return (

        <Card>

            <CardContent>

                <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 1 }}
                >
                    {title}
                </Typography>

                <Typography
                    variant="h5"
                    fontWeight="bold"
                >
                    {value}
                </Typography>

            </CardContent>

        </Card>
    );
}


export default Dashboard;