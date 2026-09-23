import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    Typography,
    LinearProgress,
} from "@mui/material";

import {
    getBudgets,
    deleteBudget,
    getBudgetProgress,
} from "../api/budget";


function BudgetList() {

    const navigate = useNavigate();

    // =========================================================
    // STATE
    // =========================================================

    const [month, setMonth] = useState(new Date().getMonth() + 1);

    const [year, setYear] = useState(new Date().getFullYear());

    const [budgets, setBudgets] = useState([]);

    const [budgetProgress, setBudgetProgress] = useState({});

    const [loading, setLoading] = useState(false);

    const [error, setError] = useState("");


    // =========================================================
    // LOAD BUDGETS
    // =========================================================

    const loadBudgets = async () => {

        try {

            setLoading(true);

            setError("");

            const data = await getBudgets({
                month,
                year,
            });

            const budgetList = Array.isArray(data)
                ? data
                : data.content || [];

            setBudgets(budgetList);


            // =================================================
            // LOAD PROGRESS FOR EACH BUDGET
            // =================================================

            const progressResults = await Promise.all(
                budgetList.map(async (budget) => {

                    try {

                        const progress =
                            await getBudgetProgress(budget.id);

                        return {
                            id: budget.id,
                            progress,
                        };

                    } catch (error) {

                        console.error(
                            `Failed to load progress for budget ${budget.id}:`,
                            error
                        );

                        return {
                            id: budget.id,
                            progress: null,
                        };
                    }
                })
            );


            const progressMap = {};

            progressResults.forEach((item) => {

                progressMap[item.id] = item.progress;

            });


            setBudgetProgress(progressMap);

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to load budgets."
            );

        } finally {

            setLoading(false);

        }

    };


    // =========================================================
    // LOAD WHEN MONTH / YEAR CHANGES
    // =========================================================

    useEffect(() => {

        loadBudgets();

    }, [month, year]);


    // =========================================================
    // DELETE BUDGET
    // =========================================================

    const handleDelete = async (id) => {

        const confirmed = window.confirm(
            "Are you sure you want to delete this budget?"
        );

        if (!confirmed) {
            return;
        }


        try {

            setError("");

            await deleteBudget(id);

            setBudgets((previousBudgets) =>
                previousBudgets.filter(
                    (budget) => budget.id !== id
                )
            );


            setBudgetProgress((previousProgress) => {

                const updatedProgress = {
                    ...previousProgress,
                };

                delete updatedProgress[id];

                return updatedProgress;

            });

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to delete budget."
            );

        }

    };


    // =========================================================
    // FORMAT CURRENCY
    // =========================================================

    const formatCurrency = (amount) => {

        return new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 2,
        }).format(amount || 0);

    };


    // =========================================================
    // FORMAT MONTH
    // =========================================================

    const getMonthName = (monthNumber) => {

        return new Date(
            2000,
            monthNumber - 1,
            1
        ).toLocaleString("en-IN", {
            month: "long",
        });

    };


    // =========================================================
    // PROGRESS VALUE
    // =========================================================

    const getProgressValue = (progress) => {

        if (!progress) {
            return 0;
        }

        const percentage =
            Number(progress.percentageUsed) || 0;

        return Math.min(Math.max(percentage, 0), 100);

    };


    // =========================================================
    // RENDER
    // =========================================================

    return (

        <Box sx={{ p: { xs: 0, sm: 1, md: 4 } }}>

            {/* ================================================= */}
            {/* HEADER */}
            {/* ================================================= */}

            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 3,
                    gap: 2,
                    flexWrap: "wrap",
                }}
            >

                <Box>

                    <Typography variant="h4">
                        Budgets
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                    >
                        Manage your monthly budgets
                    </Typography>

                </Box>


                <Button
                    variant="contained"
                    onClick={() =>
                        navigate("/budgets/new")
                    }
                >
                    + Add Budget
                </Button>

            </Box>


            {/* ================================================= */}
            {/* MONTH / YEAR FILTER */}
            {/* ================================================= */}

            <Card sx={{ mb: 3 }}>

                <CardContent>

                    <Box
                        sx={{
                            display: "flex",
                            gap: 2,
                            alignItems: "center",
                            flexWrap: "wrap",
                        }}
                    >

                        {/* MONTH */}

                        <FormControl
                            sx={{
                                minWidth: 180,
                                width: { xs: "100%", sm: "auto" },
                            }}
                        >

                            <InputLabel>
                                Month
                            </InputLabel>

                            <Select
                                value={month}
                                label="Month"
                                onChange={(event) =>
                                    setMonth(
                                        Number(
                                            event.target.value
                                        )
                                    )
                                }
                            >

                                {Array.from(
                                    { length: 12 },
                                    (_, index) => {

                                        const monthNumber =
                                            index + 1;

                                        return (
                                            <MenuItem
                                                key={monthNumber}
                                                value={monthNumber}
                                            >
                                                {
                                                    getMonthName(
                                                        monthNumber
                                                    )
                                                }
                                            </MenuItem>
                                        );

                                    }
                                )}

                            </Select>

                        </FormControl>


                        {/* YEAR */}

                        <FormControl
                            sx={{
                                minWidth: 140,
                                width: { xs: "100%", sm: "auto" },
                            }}
                        >

                            <InputLabel>
                                Year
                            </InputLabel>

                            <Select
                                value={year}
                                label="Year"
                                onChange={(event) =>
                                    setYear(
                                        Number(
                                            event.target.value
                                        )
                                    )
                                }
                            >

                                {[
                                    year - 2,
                                    year - 1,
                                    year,
                                    year + 1,
                                    year + 2,
                                ].map((yearValue) => (

                                    <MenuItem
                                        key={yearValue}
                                        value={yearValue}
                                    >
                                        {yearValue}
                                    </MenuItem>

                                ))}

                            </Select>

                        </FormControl>

                    </Box>

                </CardContent>

            </Card>


            {/* ================================================= */}
            {/* ERROR */}
            {/* ================================================= */}

            {error && (

                <Alert
                    severity="error"
                    sx={{ mb: 3 }}
                >
                    {error}
                </Alert>

            )}


            {/* ================================================= */}
            {/* LOADING */}
            {/* ================================================= */}

            {loading ? (

                <Box
                    sx={{
                        display: "flex",
                        justifyContent: "center",
                        py: 6,
                    }}
                >

                    <CircularProgress />

                </Box>

            ) : budgets.length === 0 ? (

                /* ================================================= */
                /* EMPTY STATE */
                /* ================================================= */

                <Card>

                    <CardContent>

                        <Box
                            sx={{
                                textAlign: "center",
                                py: 5,
                            }}
                        >

                            <Typography
                                variant="h6"
                                gutterBottom
                            >
                                No budgets found
                            </Typography>

                            <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{ mb: 3 }}
                            >
                                You haven't created any budgets
                                for {getMonthName(month)} {year}.
                            </Typography>

                            <Button
                                variant="contained"
                                onClick={() =>
                                    navigate("/budgets/new")
                                }
                            >
                                Create Budget
                            </Button>

                        </Box>

                    </CardContent>

                </Card>

            ) : (

                /* ================================================= */
                /* BUDGET CARDS */
                /* ================================================= */

                <Box
                    sx={{
                        display: "grid",
                        gridTemplateColumns: {
                            xs: "1fr",
                            sm: "repeat(2, 1fr)",
                            lg: "repeat(3, 1fr)",
                        },
                        gap: 3,
                    }}
                >

                    {budgets.map((budget) => {

                        const progress =
                            budgetProgress[budget.id];

                        const progressValue =
                            getProgressValue(progress);

                        const exceeded =
                            progress?.exceed || false;

                        return (

                            <Card
                                key={budget.id}
                                sx={{
                                    height: "100%",
                                    display: "flex",
                                    flexDirection: "column",
                                }}
                            >

                                <CardContent
                                    sx={{
                                        flexGrow: 1,
                                    }}
                                >

                                    {/* ================================= */}
                                    {/* CATEGORY */}
                                    {/* ================================= */}

                                    <Box
                                        sx={{
                                            display: "flex",
                                            justifyContent:
                                                "space-between",
                                            alignItems:
                                                "flex-start",
                                            mb: 2,
                                            gap: 2,
                                        }}
                                    >

                                        <Box>

                                            <Typography
                                                variant="h6"
                                            >
                                                {budget.categoryName ||
                                                    "Overall Budget"}
                                            </Typography>

                                            <Typography
                                                variant="body2"
                                                color="text.secondary"
                                            >
                                                {getMonthName(
                                                    budget.month
                                                )}{" "}
                                                {budget.year}
                                            </Typography>

                                        </Box>


                                        {/* STATUS */}

                                        <Typography
                                            variant="body2"
                                            sx={{
                                                fontWeight: 600,
                                                whiteSpace:
                                                    "nowrap",
                                                color: exceeded
                                                    ? "error.main"
                                                    : "success.main",
                                            }}
                                        >
                                            {exceeded
                                                ? "Exceeded"
                                                : "On Track"}
                                        </Typography>

                                    </Box>


                                    {/* ================================= */}
                                    {/* BUDGET AMOUNT */}
                                    {/* ================================= */}

                                    <Typography
                                        variant="body2"
                                        color="text.secondary"
                                    >
                                        Budget
                                    </Typography>

                                    <Typography
                                        variant="h5"
                                        sx={{
                                            fontWeight: 600,
                                            mb: 2,
                                        }}
                                    >
                                        {formatCurrency(
                                            progress?.budgetAmount ??
                                            budget.amount
                                        )}
                                    </Typography>


                                    {/* ================================= */}
                                    {/* SPENT */}
                                    {/* ================================= */}

                                    <Box
                                        sx={{
                                            display: "flex",
                                            justifyContent:
                                                "space-between",
                                            mb: 1,
                                        }}
                                    >

                                        <Typography
                                            variant="body2"
                                        >
                                            Spent
                                        </Typography>

                                        <Typography
                                            variant="body2"
                                            fontWeight={600}
                                        >
                                            {formatCurrency(
                                                progress?.spentAmount
                                            )}
                                        </Typography>

                                    </Box>


                                    {/* ================================= */}
                                    {/* PROGRESS */}
                                    {/* ================================= */}

                                    <LinearProgress
                                        variant="determinate"
                                        value={
                                            progressValue
                                        }
                                        sx={{
                                            height: 10,
                                            borderRadius: 5,
                                            mb: 1,
                                        }}
                                    />


                                    {/* ================================= */}
                                    {/* PERCENTAGE */}
                                    {/* ================================= */}

                                    <Box
                                        sx={{
                                            display: "flex",
                                            justifyContent:
                                                "space-between",
                                            mb: 2,
                                        }}
                                    >

                                        <Typography
                                            variant="body2"
                                            color="text.secondary"
                                        >
                                            {
                                                progress?.percentageUsed ??
                                                0
                                            }
                                            % used
                                        </Typography>

                                        <Typography
                                            variant="body2"
                                            color={
                                                exceeded
                                                    ? "error.main"
                                                    : "text.secondary"
                                            }
                                        >
                                            {exceeded
                                                ? "Over budget"
                                                : "Remaining"}
                                        </Typography>

                                    </Box>


                                    {/* ================================= */}
                                    {/* REMAINING */}
                                    {/* ================================= */}

                                    <Typography
                                        variant="h6"
                                        sx={{
                                            mb: 2,
                                            color: exceeded
                                                ? "error.main"
                                                : "success.main",
                                        }}
                                    >
                                        {formatCurrency(
                                            progress?.remainingAmount
                                        )}
                                    </Typography>

                                </CardContent>


                                {/* ================================= */}
                                {/* ACTIONS */}
                                {/* ================================= */}

                                <Box
                                    sx={{
                                        display: "flex",
                                        gap: 1,
                                        p: 2,
                                        pt: 0,
                                    }}
                                >

                                    <Button
                                        variant="outlined"
                                        fullWidth
                                        onClick={() =>
                                            navigate(
                                                `/budgets/${budget.id}/edit`
                                            )
                                        }
                                    >
                                        Edit
                                    </Button>


                                    <Button
                                        variant="outlined"
                                        color="error"
                                        fullWidth
                                        onClick={() =>
                                            handleDelete(
                                                budget.id
                                            )
                                        }
                                    >
                                        Delete
                                    </Button>

                                </Box>

                            </Card>

                        );

                    })}

                </Box>

            )}

        </Box>

    );

}


export default BudgetList;
