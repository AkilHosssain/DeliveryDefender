package app.controllers;

import app.utils.switchToScene;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import app.utils.AlertUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class RegistrationController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField addressField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label errorLabel; // Label to display error messages dynamically

    /**
     * Called when the user clicks the Register button.
     * Validates the input fields and inserts the new user into the database if valid.
     */
    public void onRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String contact = contactField.getText();
        String address = addressField.getText();
        String role = roleComboBox.getValue();

        // Validate that all required fields are filled
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || contact.isEmpty() || address.isEmpty() || role == null) {
            errorLabel.setText("All fields are required!"); // Display error on-screen
            return;
        }

        // Check if the passwords match
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match!"); // Inform the user about mismatch
            return;
        }

        // Ensure the SQLite JDBC driver is loaded
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "SQLite JDBC Driver not found.");
            return;
        }

        // Connect to the database
        // SQLite database connection URL
        String url = "jdbc:sqlite:database/DeliveryDefender.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Create the users table if it doesn't already exist
                String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "email TEXT NOT NULL UNIQUE," +
                        "password TEXT NOT NULL," +
                        "contact TEXT NOT NULL UNIQUE," +
                        "address TEXT NOT NULL," +
                        "role TEXT NOT NULL" +
                        ")";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSQL); // Execute the SQL to create the table
                }

                // Insert the new user into the database
                String insertSQL = "INSERT INTO users (name, email, password, contact, address, role) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, password); // Save the plain-text password (no encryption)
                    stmt.setString(4, contact);
                    stmt.setString(5, address);
                    stmt.setString(6, role);

                    // Execute the insertion and confirm the user is registered
                    int rowsAffected = stmt.executeUpdate();
                    errorLabel.setText("Registration successful!");
                    System.out.println("User successfully registered. Rows affected: " + rowsAffected);
                    onLoginPage();
                } catch (SQLException e) {
                    // Handle database errors
                    if (e.getMessage().contains("UNIQUE constraint failed")) {
                        errorLabel.setText("Email or contact already exists!"); // Dynamic error for duplicate values
                    } else {
                        errorLabel.setText("An unexpected error occurred. Please try again.");
                    }

                }
            }
        } catch (SQLException e) {

            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to connect to the database.");
        }
    }

    /**
     * Called when the user clicks the Login button.
     * Navigates to the Login page.
     */
    @FXML
    private void onLoginPage() {
        System.out.println("Redirecting to Registration Page");
        Scene currentScene = nameField.getScene();
        switchToScene.apply(currentScene, "/views/login.fxml");
    }
}