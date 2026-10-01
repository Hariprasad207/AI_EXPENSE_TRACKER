import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import EditNoteIcon from "@mui/icons-material/EditNote";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import SettingsIcon from "@mui/icons-material/Settings";
import NotificationsIcon from "@mui/icons-material/Notifications";


export const getNotificationIcon = (type) => {

    switch (type) {

        case "BUDGET_ALERT":
            return <WarningAmberIcon color="warning" />;

        case "AI_INSIGHT":
            return <AutoAwesomeIcon color="secondary" />;

        case "EXPENSE_REMINDER":
            return <EditNoteIcon color="primary" />;

        case "REMINDER":
            return <AccessTimeIcon color="primary" />;

        case "SYSTEM":
            return <SettingsIcon color="action" />;

        default:
            return <NotificationsIcon />;


    }

};


export const getNotificationLabel = (type) => {

    switch (type) {
        case "BUDGET_ALERT":
            return "Budget Alert";

        case "AI_INSIGHT":
            return "AI Insight";

        case "EXPENSE_REMINDER":
            return "Expense Reminder";

        case "REMINDER":
            return "Reminder";

        case "SYSTEM":
            return "System";

        default:
            return "Notification";
    }

};
