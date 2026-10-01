import { useEffect, useState } from "react";
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    FormControlLabel,
    Paper,
    Switch,
    TextField,
    Typography,
} from "@mui/material";

import {
    getNotificationSettings,
    updateNotificationSettings,
} from "../api/notificationSettings";

function NotificationSettings() {
    const [settings, setSettings] = useState({
        expenseReminderEnabled: true,
        budgetAlertEnabled: true,
        aiInsightEnabled: true,
        systemNotificationEnabled: true,
        reminderTime: "20:00",
    });

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        loadSettings();
    }, []);

    const loadSettings = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getNotificationSettings();

            setSettings({
                expenseReminderEnabled: data.expenseReminderEnabled,
                budgetAlertEnabled: data.budgetAlertEnabled,
                aiInsightEnabled: data.aiInsightEnabled,
                systemNotificationEnabled: data.systemNotificationEnabled,
                reminderTime: data.reminderTime
                    ? data.reminderTime.substring(0, 5)
                    : "20:00",
            });
        } catch (error) {
            setError(
                error.response?.data?.message ||
                "Failed to load notification settings."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleToggle = (event) => {
        const { name, checked } = event.target;

        setSettings((previous) => ({
            ...previous,
            [name]: checked,
        }));

        setSuccess("");
        setError("");
    };

    const handleTimeChange = (event) => {
        setSettings((previous) => ({
            ...previous,
            reminderTime: event.target.value,
        }));

        setSuccess("");
        setError("");
    };

    const handleSave = async () => {
        try {
            setSaving(true);
            setError("");
            setSuccess("");

            const data = await updateNotificationSettings({
                ...settings,
                reminderTime: `${settings.reminderTime}:00`,
            });

            setSettings({
                expenseReminderEnabled: data.expenseReminderEnabled,
                budgetAlertEnabled: data.budgetAlertEnabled,
                aiInsightEnabled: data.aiInsightEnabled,
                systemNotificationEnabled: data.systemNotificationEnabled,
                reminderTime: data.reminderTime
                    ? data.reminderTime.substring(0, 5)
                    : "20:00",
            });

            setSuccess("Notification settings saved successfully.");
        } catch (error) {
            setError(
                error.response?.data?.message ||
                "Failed to save notification settings."
            );
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    py: 5,
                }}
            >
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Paper
            elevation={0}
            sx={{
                p: { xs: 2, sm: 3 },
                border: "1px solid",
                borderColor: "divider",
                borderRadius: 3,
            }}
        >
            <Typography variant="h6" fontWeight={700}>
                Notification Settings
            </Typography>

            <Typography
                variant="body2"
                color="text.secondary"
                sx={{ mt: 0.5, mb: 3 }}
            >
                Choose which notifications and reminders you want to receive.
            </Typography>

            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            )}

            {success && (
                <Alert severity="success" sx={{ mb: 2 }}>
                    {success}
                </Alert>
            )}

            <Box>
                <FormControlLabel
                    control={
                        <Switch
                            name="expenseReminderEnabled"
                            checked={settings.expenseReminderEnabled}
                            onChange={handleToggle}
                        />
                    }
                    label={
                        <Box>
                            <Typography fontWeight={600}>
                                Expense Reminder
                            </Typography>
                            <Typography
                                variant="body2"
                                color="text.secondary"
                            >
                                Receive reminders to record your expenses.
                            </Typography>
                        </Box>
                    }
                    sx={{
                        width: "100%",
                        alignItems: "flex-start",
                        mb: 2,
                    }}
                />

                <FormControlLabel
                    control={
                        <Switch
                            name="budgetAlertEnabled"
                            checked={settings.budgetAlertEnabled}
                            onChange={handleToggle}
                        />
                    }
                    label={
                        <Box>
                            <Typography fontWeight={600}>
                                Budget Alerts
                            </Typography>
                            <Typography
                                variant="body2"
                                color="text.secondary"
                            >
                                Get notified when your spending approaches a budget limit.
                            </Typography>
                        </Box>
                    }
                    sx={{
                        width: "100%",
                        alignItems: "flex-start",
                        mb: 2,
                    }}
                />

                <FormControlLabel
                    control={
                        <Switch
                            name="aiInsightEnabled"
                            checked={settings.aiInsightEnabled}
                            onChange={handleToggle}
                        />
                    }
                    label={
                        <Box>
                            <Typography fontWeight={600}>
                                AI Insights
                            </Typography>
                            <Typography
                                variant="body2"
                                color="text.secondary"
                            >
                                Receive AI-powered financial insights.
                            </Typography>
                        </Box>
                    }
                    sx={{
                        width: "100%",
                        alignItems: "flex-start",
                        mb: 2,
                    }}
                />

                <FormControlLabel
                    control={
                        <Switch
                            name="systemNotificationEnabled"
                            checked={settings.systemNotificationEnabled}
                            onChange={handleToggle}
                        />
                    }
                    label={
                        <Box>
                            <Typography fontWeight={600}>
                                System Notifications
                            </Typography>
                            <Typography
                                variant="body2"
                                color="text.secondary"
                            >
                                Receive important system notifications.
                            </Typography>
                        </Box>
                    }
                    sx={{
                        width: "100%",
                        alignItems: "flex-start",
                        mb: 3,
                    }}
                />

                <TextField
                    label="Daily Reminder Time"
                    type="time"
                    value={settings.reminderTime}
                    onChange={handleTimeChange}
                    fullWidth
                    InputLabelProps={{
                        shrink: true,
                    }}
                    helperText="Time at which your expense reminder can be sent."
                    sx={{
                        maxWidth: 300,
                        "& .MuiOutlinedInput-root": {
                            borderRadius: 2,
                        },
                    }}
                />

                <Box sx={{ mt: 3 }}>
                    <Button
                        variant="contained"
                        onClick={handleSave}
                        disabled={saving}
                        sx={{
                            borderRadius: 2,
                            px: 4,
                            textTransform: "none",
                            fontWeight: 600,
                        }}
                    >
                        {saving ? (
                            <>
                                <CircularProgress
                                    size={20}
                                    sx={{ mr: 1 }}
                                />
                                Saving...
                            </>
                        ) : (
                            "Save Notification Settings"
                        )}
                    </Button>
                </Box>
            </Box>
        </Paper>
    );
}

export default NotificationSettings;