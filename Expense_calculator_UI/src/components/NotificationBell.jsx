import { useEffect, useState } from "react";

import {
    Badge,
    IconButton,
    Menu,
    Box,
    Typography,
    List,
    ListItem,
    ListItemButton,
    Divider,
    Button,
    CircularProgress,
    Stack,
} from "@mui/material";

import NotificationsIcon from "@mui/icons-material/Notifications";

import {
    getNotifications,
    getUnreadNotificationCount,
    markNotificationAsRead,
    markAllNotificationsAsRead,
} from "../api/notification";

import {
    getNotificationIcon,
    getNotificationLabel,
} from "../utils/notifictationUtils";


function NotificationBell() {

    const [anchorEl, setAnchorEl] = useState(null);

    const [notifications, setNotifications] =
        useState([]);

    const [unreadCount, setUnreadCount] =
        useState(0);

    const [loading, setLoading] =
        useState(false);


    const open = Boolean(anchorEl);


    const loadUnreadCount = async () => {

        try {

            const count =
                await getUnreadNotificationCount();

            setUnreadCount(count);

        } catch (error) {

            console.error(
                "Failed to load unread notification count",
                error
            );

        }

    };


    const loadNotifications = async () => {

        try {

            setLoading(true);

            const data =
                await getNotifications();

            setNotifications(data);

            await loadUnreadCount();

        } catch (error) {

            console.error(
                "Failed to load notifications",
                error
            );

        } finally {

            setLoading(false);

        }

    };


    useEffect(() => {

        loadUnreadCount();

    }, []);


    const handleOpen = async (event) => {

        setAnchorEl(event.currentTarget);

        await loadNotifications();

    };


    const handleClose = () => {

        setAnchorEl(null);

    };


    const handleNotificationClick =
        async (notification) => {

            try {

                if (!notification.isRead) {

                    await markNotificationAsRead(
                        notification.id
                    );


                    setNotifications((previous) =>
                        previous.map((item) =>
                            item.id === notification.id
                                ? {
                                    ...item,
                                    isRead: true,
                                }
                                : item
                        )
                    );


                    setUnreadCount((previous) =>
                        Math.max(previous - 1, 0)
                    );

                }

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


            setUnreadCount(0);

        } catch (error) {

            console.error(
                "Failed to mark all notifications as read",
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


    return (

        <>

            {/* =========================
                NOTIFICATION BUTTON
            ========================== */}

            <IconButton
                color="inherit"
                onClick={handleOpen}
            >

                <Badge
                    badgeContent={unreadCount}
                    color="error"
                >

                    <NotificationsIcon />

                </Badge>

            </IconButton>


            {/* =========================
                NOTIFICATION MENU
            ========================== */}

            <Menu

                anchorEl={anchorEl}

                open={open}

                onClose={handleClose}

                PaperProps={{
                    sx: {
                        width: 400,
                        maxHeight: 550,
                    },
                }}

            >

                {/* =========================
                    HEADER
                ========================== */}

                <Box

                    sx={{
                        display: "flex",

                        justifyContent:
                            "space-between",

                        alignItems:
                            "center",

                        px: 2,

                        py: 1.5,
                    }}

                >

                    <Typography
                        variant="h6"
                    >
                        Notifications
                    </Typography>


                    {notifications.some(
                        (notification) =>
                            !notification.isRead
                    ) && (

                            <Button

                                size="small"

                                onClick={
                                    handleMarkAllAsRead
                                }

                            >
                                Mark all read
                            </Button>

                        )}

                </Box>


                <Divider />


                {/* =========================
                    LOADING
                ========================== */}

                {loading && (

                    <Box

                        sx={{
                            display: "flex",

                            justifyContent:
                                "center",

                            p: 3,
                        }}

                    >

                        <CircularProgress
                            size={28}
                        />

                    </Box>

                )}


                {/* =========================
                    EMPTY STATE
                ========================== */}

                {!loading &&
                    notifications.length === 0 && (

                        <Box

                            sx={{
                                p: 3,

                                textAlign:
                                    "center",
                            }}

                        >

                            <Typography
                                color="text.secondary"
                            >
                                No notifications
                            </Typography>

                        </Box>

                    )}


                {/* =========================
                    NOTIFICATION LIST
                ========================== */}

                {!loading &&
                    notifications.length > 0 && (

                        <List
                            disablePadding
                        >

                            {notifications.map(
                                (notification) => (

                                    <ListItem

                                        key={
                                            notification.id
                                        }

                                        disablePadding

                                    >

                                        <ListItemButton

                                            onClick={() =>
                                                handleNotificationClick(
                                                    notification
                                                )
                                            }

                                            sx={{
                                                alignItems:
                                                    "flex-start",

                                                gap: 1.5,

                                                py: 1.5,

                                                px: 2,

                                                backgroundColor:
                                                    notification.isRead
                                                        ? "transparent"
                                                        : "action.hover",
                                            }}

                                        >


                                            {/* ICON */}

                                            <Box

                                                sx={{
                                                    mt: 0.3,

                                                    minWidth: 32,

                                                    display:
                                                        "flex",

                                                    justifyContent:
                                                        "center",
                                                }}

                                            >

                                                {getNotificationIcon(
                                                    notification.type
                                                )}

                                            </Box>


                                            {/* CONTENT */}

                                            <Box

                                                sx={{
                                                    flexGrow: 1,

                                                    minWidth: 0,
                                                }}

                                            >

                                                <Stack

                                                    direction="row"

                                                    spacing={1}

                                                    alignItems="center"

                                                >

                                                    <Typography

                                                        variant="subtitle2"

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

                                                </Stack>


                                                <Typography

                                                    variant="caption"

                                                    color="text.secondary"

                                                    sx={{
                                                        display:
                                                            "block",

                                                        mb: 0.5,
                                                    }}

                                                >

                                                    {getNotificationLabel(
                                                        notification.type
                                                    )}

                                                </Typography>


                                                <Typography

                                                    variant="body2"

                                                    color="text.secondary"

                                                    sx={{
                                                        display:
                                                            "-webkit-box",

                                                        WebkitLineClamp:
                                                            2,

                                                        WebkitBoxOrient:
                                                            "vertical",

                                                        overflow:
                                                            "hidden",
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
                                                        display:
                                                            "block",

                                                        mt: 0.75,
                                                    }}

                                                >

                                                    {formatDate(
                                                        notification.createdAt
                                                    )}

                                                </Typography>

                                            </Box>

                                        </ListItemButton>

                                    </ListItem>

                                )
                            )}

                        </List>

                    )}

            </Menu>

        </>

    );

}


export default NotificationBell;