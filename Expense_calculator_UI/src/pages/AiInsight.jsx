import { useEffect, useState } from "react";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    Divider,
    IconButton,
    Stack,
    Tooltip,
    Typography,
} from "@mui/material";

import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import DeleteIcon from "@mui/icons-material/Delete";
import DoneIcon from "@mui/icons-material/Done";
import RefreshIcon from "@mui/icons-material/Refresh";

import {
    getAiInsights,
    analyzeAiInsights,
    markAiInsightAsRead,
    deleteAiInsight,
} from "../api/aiInsight";


function AiInsights() {

    const [insights, setInsights] = useState([]);

    const [loading, setLoading] = useState(true);

    const [analyzing, setAnalyzing] = useState(false);

    const [error, setError] = useState("");

    const [success, setSuccess] = useState("");


    /* =========================
       LOAD INSIGHTS
    ========================= */

    const loadInsights = async () => {

        try {

            setLoading(true);
            setError("");

            const data = await getAiInsights();

            setInsights(data);

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to load AI insights."
            );

        } finally {

            setLoading(false);
        }
    };


    /* =========================
       INITIAL LOAD
    ========================= */

    useEffect(() => {

        loadInsights();

    }, []);


    /* =========================
       RUN AI ANALYSIS
    ========================= */

    const handleAnalyze = async () => {

        try {

            setAnalyzing(true);
            setError("");
            setSuccess("");

            await analyzeAiInsights();

            await loadInsights();

            setSuccess(
                "AI insights updated successfully."
            );

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to analyze your finances."
            );

        } finally {

            setAnalyzing(false);
        }
    };


    /* =========================
       MARK AS READ
    ========================= */

    const handleMarkAsRead = async (id) => {

        try {

            const updatedInsight =
                await markAiInsightAsRead(id);

            setInsights((currentInsights) =>
                currentInsights.map((insight) =>
                    insight.id === id
                        ? updatedInsight
                        : insight
                )
            );

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to mark insight as read."
            );
        }
    };


    /* =========================
       DELETE
    ========================= */

    const handleDelete = async (id) => {

        try {

            await deleteAiInsight(id);

            setInsights((currentInsights) =>
                currentInsights.filter(
                    (insight) => insight.id !== id
                )
            );

        } catch (error) {

            console.error(error);

            setError(
                error.response?.data?.message ||
                "Failed to delete insight."
            );
        }
    };


    /* =========================
       INSIGHT TYPE
    ========================= */

    const getInsightLabel = (type) => {

        switch (type) {

            case "HIGH_SPENDING":
                return "High Spending";

            case "SAVING":
                return "Saving";

            case "BUDGET_WARNING":
                return "Budget";

            case "SPENDING_TREND":
                return "Spending Trend";

            case "GENERAL":
                return "General";

            default:
                return type || "AI Insight";
        }
    };


    /* =========================
       INSIGHT ICON
    ========================= */

    const getInsightIcon = (type) => {

        switch (type) {

            case "HIGH_SPENDING":
                return "⚠️";

            case "SAVING":
                return "💰";

            case "BUDGET_WARNING":
                return "📊";

            case "SPENDING_TREND":
                return "📈";

            case "GENERAL":
                return "💡";

            default:
                return "🤖";
        }
    };


    /* =========================
       LOADING
    ========================= */

    if (loading) {

        return (
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    minHeight: "60vh",
                }}
            >
                <CircularProgress />
            </Box>
        );
    }


    return (
        <Box
            sx={{
                maxWidth: 1100,
                mx: "auto",
                px: 3,
                py: 4,
            }}
        >

            {/* =========================
                HEADER
            ========================= */}

            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: {
                        xs: "flex-start",
                        sm: "center",
                    },
                    flexDirection: {
                        xs: "column",
                        sm: "row",
                    },
                    gap: 2,
                    mb: 4,
                }}
            >

                <Box>

                    <Stack
                        direction="row"
                        spacing={1}
                        alignItems="center"
                    >

                        <AutoAwesomeIcon />

                        <Typography
                            variant="h4"
                            fontWeight="bold"
                        >
                            AI Insights
                        </Typography>

                    </Stack>

                    <Typography
                        color="text.secondary"
                        sx={{ mt: 1 }}
                    >
                        Understand your spending and financial
                        habits with intelligent insights.
                    </Typography>

                </Box>


                <Stack
                    direction="row"
                    spacing={1}
                >

                    <Tooltip title="Refresh">

                        <IconButton
                            onClick={loadInsights}
                            disabled={analyzing}
                        >
                            <RefreshIcon />
                        </IconButton>

                    </Tooltip>


                    <Button
                        variant="contained"
                        startIcon={
                            analyzing
                                ? <CircularProgress
                                    size={18}
                                    color="inherit"
                                />
                                : <AutoAwesomeIcon />
                        }
                        onClick={handleAnalyze}
                        disabled={analyzing}
                    >
                        {analyzing
                            ? "Analyzing..."
                            : "Analyze Finances"}
                    </Button>

                </Stack>

            </Box>


            {/* =========================
                ALERTS
            ========================= */}

            {error && (

                <Alert
                    severity="error"
                    sx={{ mb: 3 }}
                    onClose={() => setError("")}
                >
                    {error}
                </Alert>

            )}


            {success && (

                <Alert
                    severity="success"
                    sx={{ mb: 3 }}
                    onClose={() => setSuccess("")}
                >
                    {success}
                </Alert>

            )}


            {/* =========================
                EMPTY STATE
            ========================= */}

            {insights.length === 0 && (

                <Card>

                    <CardContent
                        sx={{
                            textAlign: "center",
                            py: 8,
                        }}
                    >

                        <AutoAwesomeIcon
                            sx={{
                                fontSize: 60,
                                mb: 2,
                                opacity: 0.5,
                            }}
                        />

                        <Typography
                            variant="h6"
                            fontWeight="bold"
                        >
                            No AI insights yet
                        </Typography>

                        <Typography
                            color="text.secondary"
                            sx={{
                                mt: 1,
                                mb: 3,
                            }}
                        >
                            Run an analysis to discover patterns
                            in your finances.
                        </Typography>

                        <Button
                            variant="contained"
                            startIcon={<AutoAwesomeIcon />}
                            onClick={handleAnalyze}
                            disabled={analyzing}
                        >
                            Analyze Finances
                        </Button>

                    </CardContent>

                </Card>

            )}


            {/* =========================
                INSIGHTS
            ========================= */}

            <Stack spacing={2}>

                {insights.map((insight) => (

                    <Card
                        key={insight.id}
                        elevation={insight.read ? 1 : 3}
                        sx={{
                            borderLeft: insight.read
                                ? "4px solid transparent"
                                : "4px solid",
                        }}
                    >

                        <CardContent>

                            <Box
                                sx={{
                                    display: "flex",
                                    justifyContent: "space-between",
                                    gap: 2,
                                }}
                            >

                                {/* CONTENT */}

                                <Box
                                    sx={{
                                        display: "flex",
                                        gap: 2,
                                        flexGrow: 1,
                                    }}
                                >

                                    <Typography
                                        sx={{
                                            fontSize: 32,
                                            lineHeight: 1,
                                        }}
                                    >
                                        {getInsightIcon(
                                            insight.type
                                        )}
                                    </Typography>


                                    <Box
                                        sx={{
                                            flexGrow: 1,
                                        }}
                                    >

                                        <Stack
                                            direction="row"
                                            spacing={1}
                                            alignItems="center"
                                            flexWrap="wrap"
                                        >

                                            <Typography
                                                variant="h6"
                                                fontWeight="bold"
                                            >
                                                {insight.title}
                                            </Typography>

                                            <Chip
                                                label={getInsightLabel(
                                                    insight.type
                                                )}
                                                size="small"
                                            />

                                            {!insight.read && (

                                                <Chip
                                                    label="New"
                                                    size="small"
                                                    color="primary"
                                                />

                                            )}

                                        </Stack>


                                        <Typography
                                            color="text.secondary"
                                            sx={{
                                                mt: 1,
                                                lineHeight: 1.7,
                                            }}
                                        >
                                            {insight.message}
                                        </Typography>


                                        {insight.createdAt && (

                                            <Typography
                                                variant="caption"
                                                color="text.secondary"
                                                sx={{
                                                    display: "block",
                                                    mt: 2,
                                                }}
                                            >
                                                {new Date(
                                                    insight.createdAt
                                                ).toLocaleString()}
                                            </Typography>

                                        )}

                                    </Box>

                                </Box>


                                {/* ACTIONS */}

                                <Stack
                                    direction="row"
                                    spacing={0.5}
                                    alignItems="flex-start"
                                >

                                    {!insight.read && (

                                        <Tooltip
                                            title="Mark as read"
                                        >

                                            <IconButton
                                                onClick={() =>
                                                    handleMarkAsRead(
                                                        insight.id
                                                    )
                                                }
                                            >
                                                <DoneIcon />
                                            </IconButton>

                                        </Tooltip>

                                    )}


                                    <Tooltip title="Delete">

                                        <IconButton
                                            color="error"
                                            onClick={() =>
                                                handleDelete(
                                                    insight.id
                                                )
                                            }
                                        >
                                            <DeleteIcon />
                                        </IconButton>

                                    </Tooltip>

                                </Stack>

                            </Box>

                        </CardContent>

                    </Card>

                ))}

            </Stack>

        </Box>
    );
}

export default AiInsights;