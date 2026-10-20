import { useEffect, useRef, useState } from "react";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Fab,
    IconButton,
    Paper,
    TextField,
    Tooltip,
    Typography,
} from "@mui/material";

import ChatIcon from "@mui/icons-material/Chat";
import CloseIcon from "@mui/icons-material/Close";
import SendIcon from "@mui/icons-material/Send";
import DeleteIcon from "@mui/icons-material/Delete";

import {
    sendAiChatMessage,
    getAiChatHistory,
    clearAiChatHistory,
    clearAiConversation,
} from "../api/aichat";


function AIChatBox() {

    const [open, setOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [conversationId, setConversationId] = useState(null);

    // --------------------------------------------------
    // DELETE STATE
    // --------------------------------------------------

    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteType, setDeleteType] = useState(null);
    const [deleteLoading, setDeleteLoading] = useState(false);
    const [deleteError, setDeleteError] = ("");
    const [error, setError] = useState("");
    const messagesEndRef = useRef(null);


    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({
            behavior: "smooth",
        });
    }, [messages]);

    useEffect(() => {
        if (!open) {
            return;
        }


        const loadHistory = async () => {
            try {
                setError("");
                const history = await getAiChatHistory();

                if (Array.isArray(history) && history.length > 0) {

                    setMessages(history);
                    const latestMessage = history[history.length - 1];

                    if (latestMessage?.conversationId) {
                        setConversationId(latestMessage.conversationId);
                    }
                }

            } catch (error) {
                console.error(
                    "Failed to load AI chat history:",
                    error
                );

                setError(
                    "Unable to load chat history."
                );
            }
        };

        loadHistory();
    }, [open]);


    // --------------------------------------------------
    // SEND MESSAGE
    // --------------------------------------------------

    const handleSendMessage = async () => {
        const message = input.trim();

        if (!message || loading) {
            return;
        }
        setError("");

        const userMessage = {
            role: "user",
            message: message,
        };

        setMessages((previous) => [
            ...previous,
            userMessage,
        ]);

        setInput("");
        setLoading(true);

        try {
            const response = await sendAiChatMessage(
                message,
                conversationId
            );

            if (response?.conversationId) {
                setConversationId(
                    response.conversationId
                );
            }
            const assistantMessage = {
                role: "assistant",

                message:
                    response?.message ||
                    response?.response ||
                    "No response received.",

                conversationId:
                    response?.conversationId ||
                    conversationId,

            };


            setMessages((previous) => [
                ...previous,
                assistantMessage,
            ]);

        } catch (error) {
            console.error(
                "Failed to send AI message:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Failed to get a response from the AI assistant."
            );

        } finally {
            setLoading(false);
        }
    };


    const handleKeyDown = (event) => {

        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            handleSendMessage();
        }
    };

    const requestDeleteAll = () => {
        setDeleteType("ALL");
        setDeleteError("");
        setDeleteDialogOpen(true);
    };

    const handleDeleteConfirmed = async () => {
        if (deleteLoading) {
            return;
        }


        try {

            setDeleteLoading(true);
            setDeleteError("");

            if (deleteType === "ALL") {
                await clearAiChatHistory();
            }


            else if (deleteType === "CURRENT" && conversationId) {
                await clearAiConversation(
                    conversationId
                );
            }

            setMessages([]);
            setConversationId(null);
            setInput("");
            setError("");
            setDeleteDialogOpen(false);

        } catch (error) {
            console.error(
                "Failed to delete AI chat:",
                error
            );

            setDeleteError(
                error.response?.data?.message ||
                "Failed to delete chat history."
            );

        } finally {
            setDeleteLoading(false);
        }

    };


    const handleCloseDeleteDialog = () => {
        if (deleteLoading) {
            return;
        }

        setDeleteDialogOpen(false);
        setDeleteError("");
    };

    return (
        <>
            {!open && (
                <Fab
                    color="primary"
                    aria-label="Open AI assistant"
                    onClick={() => setOpen(true)}
                    sx={{
                        position: "fixed",
                        bottom: { xs: 16, sm: 24 },
                        right: { xs: 16, sm: 24 },
                        zIndex: 1300,
                    }}
                >

                    <ChatIcon />

                </Fab>

            )}


            {open && (

                <Paper
                    elevation={8}
                    sx={{
                        position: "fixed",
                        bottom: { xs: 0, sm: 24 },
                        right: { xs: 0, sm: 24 },
                        width: { xs: "100vw", sm: 380 },
                        height: { xs: "100dvh", sm: 560 },

                        display: "flex",
                        flexDirection: "column",

                        overflow: "hidden",

                        borderRadius: { xs: 0, sm: 3 },

                        zIndex: 1300,
                    }}
                >
                    <Box
                        sx={{
                            display: "flex",

                            alignItems: "center",

                            justifyContent:
                                "space-between",

                            px: 2,

                            py: 1.5,

                            backgroundColor:
                                "primary.main",

                            color:
                                "primary.contrastText",
                        }}
                    >

                        {/* TITLE */}

                        <Box>

                            <Typography
                                variant="h6"
                                fontWeight="bold"
                            >
                                AI Financial Assistant
                            </Typography>


                            <Typography
                                variant="caption"
                            >
                                Ask about your finances
                            </Typography>

                        </Box>


                        {/* HEADER BUTTONS */}

                        <Box
                            sx={{
                                display: "flex",

                                alignItems:
                                    "center",
                            }}
                        >

                            {/* DELETE ALL */}

                            <Tooltip
                                title="Delete all conversations"
                            >

                                <IconButton
                                    aria-label="Delete all conversations"

                                    onClick={
                                        requestDeleteAll
                                    }

                                    disabled={
                                        loading ||
                                        deleteLoading
                                    }

                                    sx={{
                                        color:
                                            "inherit",
                                    }}
                                >

                                    <DeleteIcon />

                                </IconButton>

                            </Tooltip>


                            {/* CLOSE */}

                            <IconButton
                                aria-label="Close AI assistant"

                                onClick={() =>
                                    setOpen(false)
                                }

                                sx={{
                                    color:
                                        "inherit",
                                }}
                            >

                                <CloseIcon />

                            </IconButton>

                        </Box>

                    </Box>


                    {/* ==========================================
                        ERROR
                    ========================================== */}

                    {error && (

                        <Alert
                            severity="error"
                            onClose={() =>
                                setError("")
                            }

                            sx={{
                                borderRadius: 0,
                            }}
                        >
                            {error}
                        </Alert>

                    )}


                    {/* ==========================================
                        MESSAGE AREA
                    ========================================== */}

                    <Box
                        sx={{
                            flex: 1,

                            overflowY: "auto",

                            p: 2,

                            backgroundColor:
                                "#f5f6f8",
                        }}
                    >

                        {/* EMPTY STATE */}

                        {messages.length === 0 && !loading && (

                            <Box
                                sx={{
                                    height: "100%",

                                    display: "flex",

                                    flexDirection:
                                        "column",

                                    alignItems:
                                        "center",

                                    justifyContent:
                                        "center",

                                    textAlign:
                                        "center",

                                    px: 3,
                                }}
                            >

                                <ChatIcon
                                    sx={{
                                        fontSize: 48,
                                        color:
                                            "primary.main",
                                        mb: 1,
                                    }}
                                />


                                <Typography
                                    variant="h6"
                                    gutterBottom
                                >
                                    How can I help?
                                </Typography>


                                <Typography
                                    variant="body2"
                                    color="text.secondary"
                                >
                                    Ask me about your
                                    expenses, income,
                                    budgets, savings,
                                    or financial trends.
                                </Typography>

                            </Box>

                        )}


                        {/* MESSAGES */}

                        {messages.map(
                            (msg, index) => (

                                <Box
                                    key={index}

                                    sx={{
                                        display: "flex",

                                        justifyContent:
                                            msg.role ===
                                                "user"
                                                ? "flex-end"
                                                : "flex-start",

                                        mb: 1.5,
                                    }}
                                >

                                    <Box
                                        sx={{
                                            maxWidth:
                                                "80%",

                                            px: 1.5,

                                            py: 1,

                                            borderRadius:
                                                2,

                                            backgroundColor:
                                                msg.role ===
                                                    "user"
                                                    ? "primary.main"
                                                    : "white",

                                            color:
                                                msg.role ===
                                                    "user"
                                                    ? "white"
                                                    : "text.primary",

                                            boxShadow:
                                                msg.role ===
                                                    "assistant"
                                                    ? 1
                                                    : 0,

                                            whiteSpace:
                                                "pre-wrap",

                                            wordBreak:
                                                "break-word",
                                        }}
                                    >

                                        {msg.role === "assistant" ? (
                                            <Box
                                                className="ai-markdown-content"
                                                sx={{
                                                    fontSize: "0.875rem",
                                                    lineHeight: 1.6,
                                                    overflowWrap: "anywhere",
                                                    "& p": {
                                                        m: 0,
                                                        mb: 1,
                                                        "&:last-child": { mb: 0 },
                                                    },
                                                    "& h1, & h2, & h3, & h4": {
                                                        fontWeight: 700,
                                                        lineHeight: 1.3,
                                                        mt: 1.5,
                                                        mb: 0.75,
                                                    },
                                                    "& h1": { fontSize: "1.25rem" },
                                                    "& h2": { fontSize: "1.125rem" },
                                                    "& h3, & h4": { fontSize: "1rem" },
                                                    "& ul, & ol": {
                                                        pl: 2.5,
                                                        mt: 0.5,
                                                        mb: 1,
                                                    },
                                                    "& li": { mb: 0.4 },
                                                    "& li > p": { mb: 0.5 },
                                                    "& strong": { fontWeight: 700 },
                                                    "& blockquote": {
                                                        m: 0,
                                                        mb: 1,
                                                        pl: 1.5,
                                                        borderLeft: "3px solid",
                                                        borderColor: "divider",
                                                        color: "text.secondary",
                                                    },
                                                    "& table": {
                                                        width: "100%",
                                                        borderCollapse: "collapse",
                                                        display: "block",
                                                        overflowX: "auto",
                                                        mb: 1,
                                                    },
                                                    "& th, & td": {
                                                        border: "1px solid",
                                                        borderColor: "divider",
                                                        p: 0.75,
                                                        textAlign: "left",
                                                    },
                                                    "& pre": {
                                                        whiteSpace: "pre",
                                                        overflowX: "auto",
                                                        p: 1,
                                                        borderRadius: 1,
                                                        bgcolor: "action.hover",
                                                        mb: 1,
                                                    },
                                                    "& :not(pre) > code": {
                                                        px: 0.5,
                                                        py: 0.2,
                                                        borderRadius: 0.5,
                                                        bgcolor: "action.hover",
                                                        fontFamily: "monospace",
                                                    },
                                                    "& a": {
                                                        color: "primary.main",
                                                        overflowWrap: "anywhere",
                                                    },
                                                }}
                                            >
                                                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                                                    {String(msg.message ?? "")}
                                                </ReactMarkdown>
                                            </Box>
                                        ) : (
                                            <Typography
                                                variant="body2"
                                                sx={{ whiteSpace: "pre-wrap" }}
                                            >
                                                {msg.message}
                                            </Typography>
                                        )}

                                    </Box>

                                </Box>

                            )
                        )}


                        {/* LOADING */}

                        {loading && (

                            <Box
                                sx={{
                                    display: "flex",

                                    justifyContent:
                                        "flex-start",

                                    mb: 1.5,
                                }}
                            >

                                <Box
                                    sx={{
                                        px: 1.5,
                                        py: 1,

                                        borderRadius: 2,

                                        backgroundColor:
                                            "white",

                                        boxShadow: 1,

                                        display: "flex",

                                        alignItems:
                                            "center",

                                        gap: 1,
                                    }}
                                >

                                    <CircularProgress
                                        size={16}
                                    />

                                    <Typography
                                        variant="body2"
                                        color="text.secondary"
                                    >
                                        AI is thinking...
                                    </Typography>

                                </Box>

                            </Box>

                        )}


                        <div
                            ref={messagesEndRef}
                        />

                    </Box>


                    {/* ==========================================
                        INPUT AREA
                    ========================================== */}

                    <Box
                        sx={{
                            p: 1.5,

                            display: "flex",

                            gap: 1,

                            borderTop:
                                "1px solid #ddd",

                            backgroundColor:
                                "white",
                        }}
                    >

                        <TextField
                            fullWidth

                            multiline

                            maxRows={4}

                            placeholder="Ask something about your finances..."

                            value={input}

                            onChange={(event) =>
                                setInput(
                                    event.target.value
                                )
                            }

                            onKeyDown={
                                handleKeyDown
                            }

                            disabled={
                                loading ||
                                deleteLoading
                            }

                            size="small"
                        />


                        <IconButton
                            color="primary"

                            onClick={
                                handleSendMessage
                            }

                            disabled={
                                !input.trim() ||
                                loading ||
                                deleteLoading
                            }

                            sx={{
                                alignSelf:
                                    "flex-end",
                            }}
                        >

                            <SendIcon />

                        </IconButton>

                    </Box>

                </Paper>

            )}


            {/* ==================================================
                DELETE CONFIRMATION DIALOG
            ================================================== */}

            <Dialog
                open={deleteDialogOpen}

                onClose={
                    handleCloseDeleteDialog
                }

                fullWidth

                maxWidth="xs"
            >

                <DialogTitle>

                    {deleteType === "ALL"
                        ? "Delete all chat history?"
                        : "Delete current conversation?"}

                </DialogTitle>


                <DialogContent>

                    <DialogContentText>

                        {deleteType === "ALL"

                            ? "This will permanently delete all of your AI conversations. Your expenses, income, budgets, categories, and other financial data will not be affected."

                            : "This will permanently delete the current AI conversation. Your financial data will not be affected."}

                    </DialogContentText>


                    {deleteError && (

                        <Alert
                            severity="error"

                            sx={{
                                mt: 2,
                            }}
                        >
                            {deleteError}
                        </Alert>

                    )}

                </DialogContent>


                <DialogActions
                    sx={{
                        px: 3,
                        pb: 2,
                    }}
                >

                    <Button
                        onClick={
                            handleCloseDeleteDialog
                        }

                        disabled={
                            deleteLoading
                        }
                    >
                        Cancel
                    </Button>


                    <Button
                        color="error"

                        variant="contained"

                        onClick={
                            handleDeleteConfirmed
                        }

                        disabled={
                            deleteLoading
                        }
                    >

                        {deleteLoading ? (

                            <CircularProgress
                                size={20}
                                color="inherit"
                            />

                        ) : (

                            "Delete"

                        )}

                    </Button>

                </DialogActions>

            </Dialog>

        </>
    );
}


export default AIChatBox;
