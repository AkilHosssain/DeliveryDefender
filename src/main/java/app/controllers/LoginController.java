package app.controllers;

import app.utils.AlertUtil;
import app.utils.SessionManager;
import app.utils.switchToScene;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    public void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        System.out.println("Login Attempt: " + username);

        String query = "SELECT id, name, role FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/DeliveryDefender.db");
             PreparedStatement stmt = conn.prepareStatement(query)) {


            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String role = rs.getString("role");
                    String name = rs.getString("name");
                    System.out.println(name + " successfully logged in as " + role);

                    // Store the username and role in the session
                    SessionManager.setId(id);
                    SessionManager.setUsername(username);
                    SessionManager.setRole(role);

                    System.out.println("Session ID: " + SessionManager.getId());
                    System.out.println("Session Username: " + SessionManager.getUsername());
                    System.out.println("Session Role: " + SessionManager.getRole());


                    String fxmlPath = switch (role) {
                        case "Customer" -> "/views/customer/CustomerDashboard.fxml";
                        case "Driver" -> "/views/driver/DriverDashboard.fxml";
                        default -> "/views/admin/AdminDashboard.fxml";
                    };
                    loadDashboard(fxmlPath);
                } else {
                    AlertUtil.showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password!");
                }
            }
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to validate credentials. Please contact support.");
        }
    }


    public void onForgotPassword() {
        System.out.println("Forgot Password Clicked");
        try {
            Scene currentScene = usernameField.getScene();
            switchToScene.apply(currentScene, "/views/ForgotPassword.fxml"); // Use the static method from the utility
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load the Forgot Password page.");
        }
    }

    public void onRegister() {
        System.out.println("Redirecting to Registration Page");
        Scene currentScene = usernameField.getScene();
        switchToScene.apply(currentScene, "/views/Registration.fxml"); // Use the static method from the utility
    }

    private void loadDashboard(String fxmlPath) {
        try {
            Scene currentScene = usernameField.getScene();
            switchToScene.apply(currentScene, fxmlPath); // Use the static method from the utility
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Dashboard Error", "Failed to load the dashboard. Please try again.");
        }
    }
}