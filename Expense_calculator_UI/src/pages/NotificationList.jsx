import { useEffect, useState } from "react";

import {
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Divider,
    IconButton,
    Stack,
    Typography,
} from "@mui/material";

import DoneIcon from "@mui/icons-material/Done";
import DeleteIcon from "@mui/icons-material/Delete";
import MarkEmailReadIcon from "@mui/icons-material/MarkEmailRead";
import NotificationsNoneIcon from "@mui/icons-material/NotificationsNone";

import {
    getNotifications,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    deleteNotification,
} from "../api/notification";

import {
    getNotificationIcon,
    getNotificationLabel,
} from "../utils/notifictationUtils";


function NotificationList() {

    const [notifications, setNotifications] = useState([]);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");


    const loadNotifications = async () => {

        try {

            setLoading(true);

            setError("");

            const data = await getNotifications();

            setNotifications(data);

        } catch (error) {

            console.error(
                "Failed to load notifications",
                error
            );

            setError(
                error.response?.data?.message ||
                "Failed to load notifications"
            );

        } finally {

            setLoading(false);

        }

    };


    useEffect(() => {

        loadNotifications();

    }, []);


    const handleMarkAsRead = async (id) => {

        try {

            await markNotificationAsRead(id);


            setNotifications((previous) =>
                previous.map((notification) =>
                    notification.id === id
                        ? {
                            ...notification,
                            isRead: true,
                        }
                        : notification
                )
            );

        } catch (error) {

            console.error(
                "Failed to mark notification as read",
                error
            );

        }

    };


    const handleMarkAllAsRead = async () => {

        try {

            await markAllNotificationsAsRead();


            setNotifications((previous) =>
                previous.map((notification) => ({
                    ...notification,
                    isRead: true,
                }))
            );

        } catch (error) {

            console.error(
                "Failed to mark all notifications as read",
                error
            );

        }

    };


    const handleDelete = async (id) => {

        try {

            await deleteNotification(id);


            setNotifications((previous) =>
                previous.filter(
                    (notification) =>
                        notification.id !== id
                )
            );

        } catch (error) {

            console.error(
                "Failed to delete notification",
                error
            );

        }

    };


    const formatDate = (date) => {

        if (!date) {
            return "";
        }

        return new Date(date).toLocaleString(
            "en-IN",
            {
                dateStyle: "medium",
                timeStyle: "short",
            }
        );

    };


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

        <Box>

            {/* =========================
                PAGE HEADER
            ========================== */}

            <Stack
                direction={{ xs: "column", sm: "row" }}
                justifyContent="space-between"
                alignItems={{ xs: "flex-start", sm: "center" }}
                sx={{
                    mb: 3,
                    gap: 2,
                }}
            >

                <Box>

                    <Typography
                        variant="h4"
                        fontWeight="bold"
                    >
                        Notifications
                    </Typography>


                    <Typography
                        variant="body2"
                        color="text.secondary"
                    >
                        Manage your alerts and notifications
                    </Typography>

                </Box>


                {notifications.some(
                    (notification) =>
                        !notification.isRead
                ) && (

                        <Button
                            variant="outlined"
                            startIcon={
                                <MarkEmailReadIcon />
                            }
                            onClick={
                                handleMarkAllAsRead
                            }
                        >
                            Mark all as read
                        </Button>

                    )}

            </Stack>


            {/* =========================
                ERROR
            ========================== */}

            {error && (

                <Typography
                    color="error"
                    sx={{
                        mb: 2,
                    }}
                >
                    {error}
                </Typography>

            )}


            {/* =========================
                EMPTY STATE
            ========================== */}

            {notifications.length === 0 && (

                <Card>

                    <CardContent
                        sx={{
                            textAlign: "center",
                            py: 6,
                        }}
                    >

                        <NotificationsNoneIcon
                            sx={{
                                fontSize: 60,
                                color: "text.secondary",
                                mb: 2,
                            }}
                        />


                        <Typography
                            variant="h6"
                        >
                            No notifications
                        </Typography>


                        <Typography
                            color="text.secondary"
                        >
                            You don't have any notifications yet.
                        </Typography>

                    </CardContent>

                </Card>

            )}


            {/* =========================
                NOTIFICATION LIST
            ========================== */}

            {notifications.length > 0 && (

                <Card>

                    {notifications.map(
                        (
                            notification,
                            index
                        ) => (

                            <Box
                                key={notification.id}
                            >

                                <Box
                                    sx={{
                                        display: "flex",

                                        justifyContent:
                                            "space-between",

                                        alignItems:
                                            "flex-start",

                                        gap: 2,

                                        p: 2,

                                        backgroundColor:
                                            notification.isRead
                                                ? "transparent"
                                                : "action.hover",
                                    }}
                                >


                                    {/* =========================
                                        NOTIFICATION ICON
                                    ========================== */}

                                    <Box
                                        sx={{
                                            mt: 0.5,

                                            minWidth: 40,

                                            display: "flex",

                                            justifyContent:
                                                "center",
                                        }}
                                    >

                                        {getNotificationIcon(
                                            notification.type
                                        )}

                                    </Box>


                                    {/* =========================
                                        NOTIFICATION CONTENT
                                    ========================== */}

                                    <Box
                                        sx={{
                                            flexGrow: 1,
                                        }}
                                    >

                                        <Stack
                                            direction="row"

                                            alignItems="center"

                                            spacing={1}
                                        >

                                            <Typography
                                                variant="subtitle1"

                                                fontWeight={
                                                    notification.isRead
                                                        ? 400
                                                        : 700
                                                }
                                            >

                                                {
                                                    notification.title
                                                }

                                            </Typography>


                                            <Typography
                                                variant="caption"

                                                sx={{
                                                    px: 1,

                                                    py: 0.25,

                                                    borderRadius: 1,

                                                    backgroundColor:
                                                        "action.hover",

                                                    color:
                                                        "text.secondary",
                                                }}
                                            >

                                                {getNotificationLabel(
                                                    notification.type
                                                )}

                                            </Typography>

                                        </Stack>


                                        <Typography
                                            variant="body2"

                                            color="text.secondary"

                                            sx={{
                                                mt: 0.75,
                                            }}
                                        >

                                            {
                                                notification.message
                                            }

                                        </Typography>


                                        <Typography
                                            variant="caption"

                                            color="text.secondary"

                                            sx={{
                                                display: "block",

                                                mt: 1,
                                            }}
                                        >

                                            {formatDate(
                                                notification.createdAt
                                            )}

                                        </Typography>

                                    </Box>


                                    {/* =========================
                                        ACTIONS
                                    ========================== */}

                                    <Stack
                                        direction="row"
                                    >

                                        {!notification.isRead && (

                                            <IconButton

                                                onClick={() =>
                                                    handleMarkAsRead(
                                                        notification.id
                                                    )
                                                }

                                                title="Mark as read"
                                            >

                                                <DoneIcon />

                                            </IconButton>

                                        )}


                                        <IconButton

                                            onClick={() =>
                                                handleDelete(
                                                    notification.id
                                                )
                                            }

                                            title="Delete notification"
                                        >

                                            <DeleteIcon />

                                        </IconButton>

                                    </Stack>

                                </Box>


                                {index <
                                    notifications.length - 1 && (

                                        <Divider />

                                    )}

                            </Box>

                        )
                    )}

                </Card>

            )}

        </Box>

    );

}


export default NotificationList;
