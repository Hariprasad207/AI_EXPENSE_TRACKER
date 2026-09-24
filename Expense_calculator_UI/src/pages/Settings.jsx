import { useEffect, useState } from "react";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Divider,
    FormControl,
    IconButton,
    InputAdornment,
    InputLabel,
    MenuItem,
    Select,
    TextField,
    Typography,
} from "@mui/material";

import SaveIcon from "@mui/icons-material/Save";
import LockIcon from "@mui/icons-material/Lock";
import VisibilityIcon from "@mui/icons-material/Visibility";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";

import {
    getCurrentUserProfile,
    updateCurrentUserProfile,
    changePassword,
} from "../api/user";


function Settings() {

    // ==================================================
    // PROFILE STATE
    // ==================================================

    const [profile, setProfile] = useState({
        fullName: "",
        email: "",
        currency: "INR",
    });


    // ==================================================
    // PROFILE STATE
    // ==================================================

    const [loading, setLoading] =
        useState(true);

    const [saving, setSaving] =
        useState(false);

    const [error, setError] =
        useState("");

    const [success, setSuccess] =
        useState("");

    const [showPasswordForm, setShowPasswordForm] =
        useState(false);

    const [passwordData, setPasswordData] =
        useState({
            currentPassword: "",
            newPassword: "",
            confirmPassword: "",
        });


    // ==================================================
    // PASSWORD LOADING
    // ==================================================

    const [passwordLoading, setPasswordLoading] =
        useState(false);


    // ==================================================
    // PASSWORD MESSAGES
    // ==================================================

    const [passwordError, setPasswordError] =
        useState("");

    const [passwordSuccess, setPasswordSuccess] =
        useState("");

    const [showCurrentPassword, setShowCurrentPassword] =
        useState(false);

    const [showNewPassword, setShowNewPassword] =
        useState(false);

    const [showConfirmPassword, setShowConfirmPassword] =
        useState(false);

    useEffect(() => {

        const loadProfile = async () => {

            try {

                setLoading(true);

                setError("");

                const data =
                    await getCurrentUserProfile();


                setProfile({
                    fullName:
                        data.fullName || "",

                    email:
                        data.email || "",

                    currency:
                        data.currency || "INR",
                });

            } catch (error) {

                console.error(
                    "Failed to load profile:",
                    error
                );


                setError(
                    error.response?.data?.message ||
                    "Unable to load your profile."
                );

            } finally {

                setLoading(false);

            }

        };


        loadProfile();

    }, []);


    // ==================================================
    // PROFILE INPUT CHANGE
    // ==================================================

    const handleProfileChange = (event) => {

        const {
            name,
            value,
        } = event.target;


        setProfile(
            (previous) => ({
                ...previous,
                [name]: value,
            })
        );


        setSuccess("");

        setError("");

    };


    // ==================================================
    // SAVE PROFILE
    // ==================================================

    const handleSaveProfile = async (event) => {

        event.preventDefault();


        try {

            setSaving(true);

            setError("");

            setSuccess("");


            const updatedProfile =
                await updateCurrentUserProfile(
                    profile
                );


            setProfile({
                fullName:
                    updatedProfile.fullName ||
                    "",

                email:
                    updatedProfile.email ||
                    "",

                currency:
                    updatedProfile.currency ||
                    "INR",
            });


            setSuccess(
                "Profile updated successfully."
            );

        } catch (error) {

            console.error(
                "Failed to update profile:",
                error
            );


            setError(
                error.response?.data?.message ||
                "Unable to update your profile."
            );

        } finally {

            setSaving(false);

        }

    };


    // ==================================================
    // PASSWORD INPUT CHANGE
    // ==================================================

    const handlePasswordChange = (event) => {

        const {
            name,
            value,
        } = event.target;


        setPasswordData(
            (previous) => ({
                ...previous,
                [name]: value,
            })
        );


        setPasswordError("");

        setPasswordSuccess("");

    };


    // ==================================================
    // CHANGE PASSWORD
    // ==================================================

    const handleChangePassword = async (event) => {

        event.preventDefault();


        setPasswordError("");

        setPasswordSuccess("");


        // ----------------------------------------------
        // FRONTEND VALIDATION
        // ----------------------------------------------

        if (
            passwordData.newPassword !==
            passwordData.confirmPassword
        ) {

            setPasswordError(
                "New password and confirm password do not match."
            );

            return;
        }


        if (
            passwordData.newPassword.length < 8
        ) {

            setPasswordError(
                "New password must be at least 8 characters long."
            );

            return;
        }


        try {

            setPasswordLoading(true);


            const response =
                await changePassword(
                    passwordData
                );


            setPasswordSuccess(
                response ||
                "Password changed successfully."
            );


            // Clear password fields

            setPasswordData({
                currentPassword: "",
                newPassword: "",
                confirmPassword: "",
            });


            // Close form after successful change

            setShowPasswordForm(false);

        } catch (error) {

            console.error(
                "Failed to change password:",
                error
            );


            setPasswordError(
                error.response?.data?.message ||
                "Unable to change your password."
            );

        } finally {

            setPasswordLoading(false);

        }

    };


    // ==================================================
    // CANCEL PASSWORD FORM
    // ==================================================

    const handleCancelPasswordChange = () => {

        setPasswordData({
            currentPassword: "",
            newPassword: "",
            confirmPassword: "",
        });

        setPasswordError("");

        setPasswordSuccess("");

        setShowPasswordForm(false);

    };


    // ==================================================
    // LOADING SCREEN
    // ==================================================

    if (loading) {

        return (

            <Box
                sx={{
                    display: "flex",

                    justifyContent:
                        "center",

                    alignItems:
                        "center",

                    minHeight:
                        "60vh",
                }}
            >

                <CircularProgress />

            </Box>

        );

    }


    // ==================================================
    // PAGE
    // ==================================================

    return (

        <Box>

            {/* ==========================================
                PAGE HEADER
            ========================================== */}

            <Box
                sx={{
                    mb: 3,
                }}
            >

                <Typography
                    variant="h4"
                    fontWeight="bold"
                >
                    Settings
                </Typography>


                <Typography
                    variant="body1"
                    color="text.secondary"
                    sx={{
                        mt: 0.5,
                    }}
                >
                    Manage your profile and
                    account preferences.
                </Typography>

            </Box>


            {/* ==========================================
                PROFILE CARD
            ========================================== */}

            <Card
                elevation={2}
                sx={{
                    maxWidth: 800,
                }}
            >

                <CardContent
                    sx={{
                        p: 3,
                    }}
                >

                    <Typography
                        variant="h6"
                        fontWeight="bold"
                    >
                        Profile
                    </Typography>


                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mt: 0.5,
                            mb: 3,
                        }}
                    >
                        Update your personal
                        information.
                    </Typography>


                    <Divider
                        sx={{
                            mb: 3,
                        }}
                    />


                    {/* PROFILE ERROR */}

                    {error && (

                        <Alert
                            severity="error"
                            sx={{
                                mb: 2,
                            }}
                        >
                            {error}
                        </Alert>

                    )}


                    {/* PROFILE SUCCESS */}

                    {success && (

                        <Alert
                            severity="success"
                            sx={{
                                mb: 2,
                            }}
                        >
                            {success}
                        </Alert>

                    )}


                    {/* PROFILE FORM */}

                    <Box
                        component="form"
                        onSubmit={
                            handleSaveProfile
                        }
                    >

                        {/* FULL NAME */}

                        <TextField
                            fullWidth
                            label="Full Name"
                            name="fullName"
                            value={
                                profile.fullName
                            }
                            onChange={
                                handleProfileChange
                            }
                            margin="normal"
                            required
                        />


                        {/* EMAIL */}

                        <TextField
                            fullWidth
                            label="Email"
                            name="email"
                            type="email"
                            value={
                                profile.email
                            }
                            onChange={
                                handleProfileChange
                            }
                            margin="normal"
                            required
                        />


                        {/* CURRENCY */}

                        <FormControl
                            fullWidth
                            margin="normal"
                        >

                            <InputLabel>
                                Currency
                            </InputLabel>

                            <Select name="currency" value={profile.currency} label="Currency" onChange={handleProfileChange} >
                                <MenuItem value="INR"> INR - Indian Rupee (₹) </MenuItem>
                                <MenuItem value="USD"> USD - US Dollar ($) </MenuItem>
                                <MenuItem value="EUR"> EUR - Euro (€) </MenuItem>
                                <MenuItem value="GBP"> GBP - British Pound (£) </MenuItem>
                                <MenuItem value="JPY"> JPY - Japanese Yen (¥) </MenuItem>
                                <MenuItem value="CAD"> CAD - Canadian Dollar (C$) </MenuItem>
                                <MenuItem value="AUD"> AUD - Australian Dollar (A$) </MenuItem>
                                <MenuItem value="CHF"> CHF - Swiss Franc (CHF) </MenuItem>
                                <MenuItem value="SGD"> SGD - Singapore Dollar (S$) </MenuItem>
                                <MenuItem value="AED"> AED - UAE Dirham (AED) </MenuItem>
                                <MenuItem value="CNY"> CNY - Chinese Yuan (¥) </MenuItem>
                            </Select>

                        </FormControl>


                        {/* SAVE */}

                        <Box
                            sx={{
                                display: "flex",

                                justifyContent:
                                    "flex-end",

                                mt: 3,
                            }}
                        >

                            <Button
                                type="submit"

                                variant="contained"

                                startIcon={
                                    saving
                                        ? (
                                            <CircularProgress
                                                size={18}
                                                color="inherit"
                                            />
                                        )
                                        : (
                                            <SaveIcon />
                                        )
                                }

                                disabled={
                                    saving
                                }
                            >

                                {saving
                                    ? "Saving..."
                                    : "Save Changes"}

                            </Button>

                        </Box>

                    </Box>

                </CardContent>

            </Card>


            {/* ==========================================
                SECURITY CARD
            ========================================== */}

            <Card
                elevation={2}

                sx={{
                    maxWidth: 800,
                    mt: 3,
                }}
            >

                <CardContent
                    sx={{
                        p: 3,
                    }}
                >

                    <Typography
                        variant="h6"
                        fontWeight="bold"
                    >
                        Security
                    </Typography>


                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mt: 0.5,
                        }}
                    >
                        Change your password
                        and manage account security.
                    </Typography>


                    <Divider
                        sx={{
                            my: 2,
                        }}
                    />


                    {/* PASSWORD SUCCESS */}

                    {passwordSuccess && (

                        <Alert
                            severity="success"
                            sx={{
                                mb: 2,
                            }}
                        >
                            {passwordSuccess}
                        </Alert>

                    )}


                    {/* PASSWORD ERROR */}

                    {passwordError && (

                        <Alert
                            severity="error"
                            sx={{
                                mb: 2,
                            }}
                        >
                            {passwordError}
                        </Alert>

                    )}


                    {/* ==================================
                        CHANGE PASSWORD BUTTON
                    ================================== */}

                    {!showPasswordForm && (

                        <Button
                            variant="outlined"

                            startIcon={
                                <LockIcon />
                            }

                            onClick={() => {

                                setPasswordError("");

                                setPasswordSuccess("");

                                setShowPasswordForm(
                                    true
                                );

                            }}
                        >
                            Change Password
                        </Button>

                    )}


                    {/* ==================================
                        PASSWORD FORM
                    ================================== */}

                    {showPasswordForm && (

                        <Box
                            component="form"
                            onSubmit={
                                handleChangePassword
                            }
                        >

                            {/* CURRENT PASSWORD */}

                            <TextField
                                fullWidth

                                label="Current Password"

                                name="currentPassword"

                                type={
                                    showCurrentPassword
                                        ? "text"
                                        : "password"
                                }

                                value={
                                    passwordData.currentPassword
                                }

                                onChange={
                                    handlePasswordChange
                                }

                                margin="normal"

                                required

                                autoComplete="current-password"

                                slotProps={{
                                    input: {
                                    endAdornment: (

                                        <InputAdornment
                                            position="end"
                                        >

                                            <IconButton
                                                onClick={() =>
                                                    setShowCurrentPassword(
                                                        (previous) =>
                                                            !previous
                                                    )
                                                }

                                                edge="end"
                                            >

                                                {showCurrentPassword
                                                    ? <VisibilityOffIcon />
                                                    : <VisibilityIcon />}

                                            </IconButton>

                                        </InputAdornment>
                                    ),
                                    },
                                }}
                            />


                            {/* NEW PASSWORD */}

                            <TextField
                                fullWidth

                                label="New Password"

                                name="newPassword"

                                type={
                                    showNewPassword
                                        ? "text"
                                        : "password"
                                }

                                value={
                                    passwordData.newPassword
                                }

                                onChange={
                                    handlePasswordChange
                                }

                                margin="normal"

                                required

                                autoComplete="new-password"

                                helperText="Minimum 8 characters"

                                slotProps={{
                                    input: {
                                    endAdornment: (

                                        <InputAdornment
                                            position="end"
                                        >

                                            <IconButton
                                                onClick={() =>
                                                    setShowNewPassword(
                                                        (previous) =>
                                                            !previous
                                                    )
                                                }

                                                edge="end"
                                            >

                                                {showNewPassword
                                                    ? <VisibilityOffIcon />
                                                    : <VisibilityIcon />}

                                            </IconButton>

                                        </InputAdornment>
                                    ),
                                    },
                                }}
                            />


                            {/* CONFIRM PASSWORD */}

                            <TextField
                                fullWidth

                                label="Confirm New Password"

                                name="confirmPassword"

                                type={
                                    showConfirmPassword
                                        ? "text"
                                        : "password"
                                }

                                value={
                                    passwordData.confirmPassword
                                }

                                onChange={
                                    handlePasswordChange
                                }

                                margin="normal"

                                required

                                autoComplete="new-password"

                                slotProps={{
                                    input: {
                                    endAdornment: (

                                        <InputAdornment
                                            position="end"
                                        >

                                            <IconButton
                                                onClick={() =>
                                                    setShowConfirmPassword(
                                                        (previous) =>
                                                            !previous
                                                    )
                                                }

                                                edge="end"
                                            >

                                                {showConfirmPassword
                                                    ? <VisibilityOffIcon />
                                                    : <VisibilityIcon />}

                                            </IconButton>

                                        </InputAdornment>
                                    ),
                                    },
                                }}
                            />


                            {/* PASSWORD BUTTONS */}

                            <Box
                                sx={{
                                    display: "flex",

                                    justifyContent:
                                        "flex-end",

                                    gap: 2,

                                    mt: 3,
                                }}
                            >

                                <Button
                                    variant="outlined"

                                    onClick={
                                        handleCancelPasswordChange
                                    }

                                    disabled={
                                        passwordLoading
                                    }
                                >
                                    Cancel
                                </Button>


                                <Button
                                    type="submit"

                                    variant="contained"

                                    startIcon={
                                        passwordLoading
                                            ? (
                                                <CircularProgress
                                                    size={18}
                                                    color="inherit"
                                                />
                                            )
                                            : (
                                                <LockIcon />
                                            )
                                    }

                                    disabled={
                                        passwordLoading
                                    }
                                >

                                    {passwordLoading
                                        ? "Changing..."
                                        : "Change Password"}

                                </Button>

                            </Box>

                        </Box>

                    )}

                </CardContent>

            </Card>

        </Box>

    );
}


export default Settings;
