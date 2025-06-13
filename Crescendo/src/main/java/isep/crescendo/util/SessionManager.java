package isep.crescendo.util;

import isep.crescendo.model.User;

public class SessionManager {
    private static User currentUser;
    private static boolean isAdminSession = false;

    public static void setCurrentUser(User user) {
        currentUser = user;
        isAdminSession = (user != null && user.isAdmin());
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
        isAdminSession = false;
    }
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    public static boolean isAdminSession() {
        return isAdminSession;
    }
}
