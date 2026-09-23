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
import { useAuth } from "../../context/AuthContext";

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
                },
            }}
        >
            {/* LOGO / APP NAME */}

            <Toolbar>
                <Typography
                    variant="h6"
                    fontWeight="bold"
                    noWrap
                >
                    Personal Finance
                </Typography>
            </Toolbar>

            <Divider />

            {/* MENU */}

            <List sx={{ px: 1 }}>

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
                                    borderRadius: 2,
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

            <List sx={{ px: 1 }}>

                <ListItem disablePadding>

                    <ListItemButton
                        onClick={handleLogout}
                        sx={{
                            borderRadius: 2,
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
            <Toolbar>
                <Typography variant="h6" fontWeight="bold" noWrap>Personal Finance</Typography>
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
