package app.utils;

public class SessionManager {
    private static String id;
    private static String username;
    private static String role;

    // Private constructor to prevent instantiation
    private SessionManager() {}

    public static String getId() {return id;}
    public static void setId(String id) {SessionManager.id = id;}

    // Getter and Setter for Username
    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        SessionManager.username = username;
    }

    // Getter and Setter for Role
    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        SessionManager.role = role;
    }

    // Method to clear session data (optional, for logout)
    public static void clearSession() {
        username = null;
        role = null;
    }
}