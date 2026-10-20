import React from "react";
import ReactDOM from "react-dom/client";
import { createTheme, CssBaseline, ThemeProvider } from "@mui/material";

import App from "./App";
import "./index.css";
import "./App.css";
import { AuthProvider } from "./context/AuthContext.jsx";

const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "#14856f", dark: "#0d6656", light: "#e3f3ee", contrastText: "#ffffff" },
    secondary: { main: "#e6a34a", dark: "#bc7626", light: "#fff3df" },
    background: { default: "#f4f7f6", paper: "#ffffff" },
    text: { primary: "#172b3a", secondary: "#6e7f7a" },
    divider: "#e7eeeb",
    success: { main: "#168263" },
    error: { main: "#d45d58" },
    warning: { main: "#d18a2d" },
    info: { main: "#4386a2" },
  },
  typography: {
    fontFamily: "'DM Sans', sans-serif",
    h1: { fontFamily: "'Manrope', sans-serif", fontWeight: 800, letterSpacing: "-0.04em" },
    h2: { fontFamily: "'Manrope', sans-serif", fontWeight: 800, letterSpacing: "-0.035em" },
    h3: { fontFamily: "'Manrope', sans-serif", fontWeight: 800, letterSpacing: "-0.03em" },
    h4: { fontFamily: "'Manrope', sans-serif", fontWeight: 800, letterSpacing: "-0.03em" },
    h5: { fontFamily: "'Manrope', sans-serif", fontWeight: 700, letterSpacing: "-0.02em" },
    h6: { fontFamily: "'Manrope', sans-serif", fontWeight: 700, letterSpacing: "-0.015em" },
    button: { textTransform: "none", fontWeight: 700 },
  },
  shape: { borderRadius: 14 },
  components: {
    MuiCssBaseline: { styleOverrides: { body: { backgroundColor: "#f4f7f6" } } },
    MuiPaper: { styleOverrides: { rounded: { borderRadius: 16 } } },
    MuiCard: { styleOverrides: { root: { border: "1px solid #e7eeeb", boxShadow: "0 5px 24px rgba(29, 65, 54, 0.045)" } } },
    MuiCardContent: { styleOverrides: { root: { "&:last-child": { paddingBottom: 24 } } } },
    MuiButton: { styleOverrides: { root: { borderRadius: 10, paddingInline: 16, minHeight: 40 }, contained: { boxShadow: "0 4px 10px rgba(20, 133, 111, 0.18)", "&:hover": { boxShadow: "0 7px 16px rgba(20, 133, 111, 0.24)" } } } },
    MuiOutlinedInput: { styleOverrides: { root: { borderRadius: 10, backgroundColor: "#fff", "& fieldset": { borderColor: "#dce7e2" }, "&:hover fieldset": { borderColor: "#9bbdb2" } } } },
    MuiTableCell: { styleOverrides: { head: { color: "#6e7f7a", fontWeight: 700, backgroundColor: "#f7faf9" } } },
    MuiAlert: { styleOverrides: { root: { borderRadius: 12 } } },
  },
});

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <App />
      </AuthProvider>
    </ThemeProvider>
  </React.StrictMode>
);
