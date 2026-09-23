import {
    AppBar,
    Box,
    IconButton,
    Toolbar,
    Typography,
} from "@mui/material";
import { useState } from "react";
import MenuIcon from "@mui/icons-material/Menu";

import { Outlet } from "react-router-dom";

import Sidebar from "./Sidebar";
import NotificationBell from "../NotificationBell";
import AIChatBox from "../AiChatBox";


const drawerWidth = 250;


function AppLayout() {

    const [mobileOpen, setMobileOpen] = useState(false);

    return (

        <Box
            sx={{
                display: "flex",
            }}
        >

            <Sidebar
                mobileOpen={mobileOpen}
                onMobileClose={() => setMobileOpen(false)}
            />

            <AppBar
                position="fixed"

                sx={{
                    width: { xs: "100%", md: `calc(100% - ${drawerWidth}px)` },
                    ml: { xs: 0, md: `${drawerWidth}px` },
                }}
            >

                <Toolbar>

                    <IconButton
                        color="inherit"
                        edge="start"
                        aria-label="Open navigation menu"
                        onClick={() => setMobileOpen(true)}
                        sx={{ mr: 1, display: { md: "none" } }}
                    >
                        <MenuIcon />
                    </IconButton>

                    <Typography
                        variant="h6"
                        component="div"

                        sx={{
                            flexGrow: 1,
                        }}
                    >
                        Personal Finance
                    </Typography>


                    <NotificationBell />

                </Toolbar>

            </AppBar>

            <Box
                component="main"

                sx={{
                    flexGrow: 1,

                    width: { xs: "100%", md: `calc(100% - ${drawerWidth}px)` },

                    minHeight: "100vh",

                    backgroundColor:
                        "#f5f6f8",
                }}
            >
                <Toolbar />


                <Box
                    sx={{
                        p: { xs: 1.5, sm: 2, md: 3 },
                    }}
                >

                    <Outlet />

                </Box>

            </Box>

            <AIChatBox />

        </Box>

    );
}


export default AppLayout;
