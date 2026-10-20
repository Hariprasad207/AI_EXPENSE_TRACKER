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
                    backgroundColor: "rgba(255,255,255,0.94)",
                    color: "#172b3a",
                    boxShadow: "0 3px 18px rgba(29, 65, 54, 0.055)",
                    borderBottom: "1px solid #e7eeeb",
                    backdropFilter: "blur(12px)",
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
                            fontWeight: 700,
                            letterSpacing: "-0.02em",
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
                        "#f4f7f6",
                    backgroundImage: "radial-gradient(ellipse at 85% 0%, rgba(20,133,111,0.055), transparent 34%)",
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
