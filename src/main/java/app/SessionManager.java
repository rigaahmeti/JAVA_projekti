package app;

public class SessionManager {
    private static String currentUser;
    private static String currentRole;

    private SessionManager() {}

    public static void login(String username, String role) {
        currentUser = username;
        currentRole = role;
    }

    public static void logout() {
        currentUser = null;
        currentRole = null;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentRole;
    }
}

