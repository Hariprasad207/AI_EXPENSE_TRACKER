import {
    AppBar,
    Box,
    Toolbar,
    Typography,
} from "@mui/material";

import { Outlet } from "react-router-dom";

import Sidebar from "./Sidebar";
import NotificationBell from "../NotificationBell";


const drawerWidth = 250;


function AppLayout() {

    return (

        <Box
            sx={{
                display: "flex",
            }}
        >

            <Sidebar />


            {/* TOP NAVBAR */}

            <AppBar
                position="fixed"

                sx={{
                    width:
                        `calc(100% - ${drawerWidth}px)`,

                    ml:
                        `${drawerWidth}px`,
                }}
            >

                <Toolbar>

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


            {/* MAIN CONTENT */}

            <Box
                component="main"

                sx={{
                    flexGrow: 1,

                    width:
                        `calc(100% - ${drawerWidth}px)`,

                    minHeight: "100vh",

                    backgroundColor:
                        "#f5f6f8",
                }}
            >

                {/* SPACE FOR APPBAR */}

                <Toolbar />


                <Box
                    sx={{
                        p: 3,
                    }}
                >

                    <Outlet />

                </Box>

            </Box>

        </Box>

    );
}


export default AppLayout;