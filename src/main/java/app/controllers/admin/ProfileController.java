package app.controllers.admin;

import app.utils.AlertUtil;
import app.utils.SessionManager;
import app.utils.switchToScene;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.sql.*;

public class ProfileController {

    @FXML
    private Label nameLabel, emailLabel, contactLabel, addressLabel;

    private static final String DB_URL = "jdbc:sqlite:database/DeliveryDefender.db";

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        String userId = SessionManager.getId();

        if (userId == null || userId.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Session Error", "User session ID is missing.");
            return;
        }

        String sql = "SELECT name, email, contact, address FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));
                emailLabel.setText(rs.getString("email"));
                contactLabel.setText(rs.getString("contact"));
                addressLabel.setText(rs.getString("address"));
            } else {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Profile Error", "User profile not found.");
            }
        } catch (SQLException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load user profile.");
        }
    }
    @FXML
    private void onBackClick() {
        Scene currentScene = nameLabel.getScene(); // Get current scene from any UI element
        switchToScene.apply(currentScene, "/views/admin/AdminDashboard.fxml");
    }
}