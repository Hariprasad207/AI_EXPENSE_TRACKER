import {
    Box,
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Toolbar,
    Typography,
    Divider,
} from "@mui/material";

import DashboardIcon from "@mui/icons-material/Dashboard";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet";
import CategoryIcon from "@mui/icons-material/Category";
import SavingsIcon from "@mui/icons-material/Savings";
import NotificationsIcon from "@mui/icons-material/Notifications";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import SettingsIcon from "@mui/icons-material/Settings";
import LogoutIcon from "@mui/icons-material/Logout";

import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/useAuth";

const drawerWidth = 250;

const menuItems = [
    {
        label: "Dashboard",
        path: "/dashboard",
        icon: <DashboardIcon />,
    },
    {
        label: "Expenses",
        path: "/expenses",
        icon: <ReceiptLongIcon />,
    },
    {
        label: "Income",
        path: "/income",
        icon: <AccountBalanceWalletIcon />,
    },
    {
        label: "Categories",
        path: "/categories",
        icon: <CategoryIcon />,
    },
    {
        label: "Budgets",
        path: "/budgets",
        icon: <SavingsIcon />,
    },
    {
        label: "Notifications",
        path: "/notifications",
        icon: <NotificationsIcon />,
    },
    {
        label: "AI Insights",
        path: "/ai-insights",
        icon: <AutoAwesomeIcon />,
    },
    {
        label: "Settings",
        path: "/settings",
        icon: <SettingsIcon />,
    },
];

function Sidebar({ mobileOpen, onMobileClose }) {
    const location = useLocation();
    const { logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    return (
        <>
        <Drawer
            variant="permanent"
            sx={{
                width: drawerWidth,
                flexShrink: 0,
                display: { xs: "none", md: "block" },

                "& .MuiDrawer-paper": {
                    width: drawerWidth,
                    boxSizing: "border-box",
                    borderRight: "1px solid #e7eeeb",
                    background: "linear-gradient(180deg, #ffffff 0%, #fbfdfc 100%)",
                    boxShadow: "8px 0 30px rgba(29, 65, 54, 0.025)",
                },
            }}
        >
            {/* LOGO / APP NAME */}

            <Toolbar sx={{ minHeight: "76px !important", px: 2.5 }}>
                <Typography
                    variant="h6"
                    fontWeight="bold"
                    noWrap
                    sx={{ color: "#124f45", letterSpacing: "-0.04em" }}
                >
                    Personal Finance
                </Typography>
            </Toolbar>

            <Divider />

            {/* MENU */}

            <List sx={{ px: 1.5, pt: 2 }}>

                {menuItems.map((item) => {

                    const isActive =
                        location.pathname === item.path;

                    return (
                        <ListItem
                            key={item.path}
                            disablePadding
                            sx={{ mb: 0.5 }}
                        >
                            <ListItemButton
                                component={Link}
                                to={item.path}
                                selected={isActive}
                                sx={{
                                    borderRadius: 2.5,
                                    py: 1.1,
                                    color: "#536761",
                                    "& .MuiListItemIcon-root": { minWidth: 38, color: "#81928c" },
                                    "&:hover": { backgroundColor: "#f0f7f4", color: "#126f5e" },
                                    "&.Mui-selected": { backgroundColor: "#e5f3ee", color: "#126f5e", fontWeight: 700 },
                                    "&.Mui-selected .MuiListItemIcon-root": { color: "#14856f" },
                                    "&.Mui-selected:hover": { backgroundColor: "#dcefe8" },
                                }}
                            >

                                <ListItemIcon>
                                    {item.icon}
                                </ListItemIcon>

                                <ListItemText
                                    primary={item.label}
                                />

                            </ListItemButton>
                        </ListItem>
                    );
                })}

            </List>

            <Box sx={{ flexGrow: 1 }} />

            {/* LOGOUT */}

            <Divider />

            <List sx={{ px: 1.5, py: 1.5 }}>

                <ListItem disablePadding>

                    <ListItemButton
                        onClick={handleLogout}
                        sx={{
                            borderRadius: 2.5,
                            color: "#8b625e",
                            "&:hover": { backgroundColor: "#fff0ee", color: "#c1514b" },
                        }}
                    >

                        <ListItemIcon>
                            <LogoutIcon />
                        </ListItemIcon>

                        <ListItemText
                            primary="Logout"
                        />

                    </ListItemButton>

                </ListItem>

            </List>

        </Drawer>

        <Drawer
            variant="temporary"
            open={mobileOpen}
            onClose={onMobileClose}
            ModalProps={{ keepMounted: true }}
            sx={{ display: { xs: "block", md: "none" }, "& .MuiDrawer-paper": { width: drawerWidth } }}
        >
            <Toolbar sx={{ minHeight: "76px !important", px: 2.5 }}>
                <Typography variant="h6" fontWeight="bold" noWrap sx={{ color: "#124f45", letterSpacing: "-0.04em" }}>Personal Finance</Typography>
            </Toolbar>
            <Divider />
            <List sx={{ px: 1 }}>
                {menuItems.map((item) => (
                    <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
                        <ListItemButton component={Link} to={item.path} selected={location.pathname === item.path} onClick={onMobileClose} sx={{ borderRadius: 2 }}>
                            <ListItemIcon>{item.icon}</ListItemIcon>
                            <ListItemText primary={item.label} />
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
            <Box sx={{ flexGrow: 1 }} />
            <Divider />
            <List sx={{ px: 1 }}>
                <ListItem disablePadding>
                    <ListItemButton onClick={handleLogout} sx={{ borderRadius: 2 }}>
                        <ListItemIcon><LogoutIcon /></ListItemIcon>
                        <ListItemText primary="Logout" />
                    </ListItemButton>
                </ListItem>
            </List>
        </Drawer>
        </>
    );
}

export default Sidebar;
