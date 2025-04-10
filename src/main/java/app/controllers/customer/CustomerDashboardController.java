package app.controllers.customer;

import app.utils.AlertUtil;
import app.utils.SessionManager;
import app.utils.switchToScene;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerDashboardController {

    @FXML private Label contentLabel;
    @FXML private Label orderNumberLabel;
    @FXML private Label etaCountdownLabel;
    @FXML private Label statusLabel;
    @FXML private Label driverNameLabel;
    @FXML private Label driverContactLabel;
    @FXML private Label deliveryAddressLabel;
    @FXML private TextArea feedbackArea;

    private static final String DATABASE_URL = "jdbc:sqlite:database/DeliveryDefender.db";

    /** Initialize the controller and fetch order data */
    @FXML
    private void initialize() {
        System.out.println("[DEBUG] CustomerDashboardController initialized.");
        setWelcomeMessage(SessionManager.getUsername());
        loadOrderData(SessionManager.getId());
    }

    /** Set welcome message */
    private void setWelcomeMessage(String customerName) {
        contentLabel.setText((customerName == null || customerName.trim().isEmpty())
                ? "Welcome to Your Dashboard!"
                : "Welcome " + customerName + "!");
    }

    /** Fetch active orders from the database */
    private void loadOrderData(String customerId) {
        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() {
                String query = """
                        SELECT order_number, delivery_date, time_frame, status, driver, phone_number, address
                        FROM orderlist
                        WHERE customer_id = ? AND (status = 'In Progress' OR status = 'Pending')
                        """;

                try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setString(1, customerId);
                    System.out.println("[DEBUG] Executing query for Customer ID: " + customerId);

                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String orderNumber = rs.getString("order_number");
                        String deliveryDate = rs.getString("delivery_date");
                        String timeFrame = rs.getString("time_frame");
                        String status = rs.getString("status");
                        String driver = rs.getString("driver");
                        String phoneNumber = rs.getString("phone_number");
                        String address = rs.getString("address");

                        rs.close(); // Close ResultSet before updating UI

                        Platform.runLater(() -> {
                            orderNumberLabel.setText(orderNumber);
                            etaCountdownLabel.setText(deliveryDate + " (" + timeFrame + ")");
                            statusLabel.setText(status);
                            driverNameLabel.setText(driver);
                            driverContactLabel.setText(phoneNumber);
                            deliveryAddressLabel.setText(address);
                            System.out.println("[DEBUG] Order data successfully loaded.");
                        });
                    } else {
                        Platform.runLater(() -> AlertUtil.showAlert(Alert.AlertType.INFORMATION, "No Orders", "No active orders found."));
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Database error: " + e.getMessage());
                    Platform.runLater(() -> AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load order data."));
                }
                return null;
            }
        };
        new Thread(loadDataTask).start();
    }

    /** Handle scene navigation */
    private void switchScene(String fxmlPath) {
        try {
            Scene currentScene = contentLabel.getScene();
            switchToScene.apply(currentScene, fxmlPath);
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load " + fxmlPath);
        }
    }

    /** Button Actions */
    @FXML private void onAccountClick(ActionEvent event) { System.out.println("[DEBUG] MyAccount clicked."); }
    @FXML private void onCreateOrderClick(ActionEvent event) { switchScene("/views/customer/customerTaskCreateForm.fxml"); }
    @FXML private void onTrackOrderClick(ActionEvent actionEvent) { System.out.println("[DEBUG] Track Order clicked."); }
    @FXML private void onCancelOrderClick(ActionEvent actionEvent) { System.out.println("[DEBUG] Cancel Order clicked."); }
    @FXML private void onSubmitFeedbackClick(ActionEvent actionEvent) {
        String feedback = feedbackArea.getText();
        System.out.println("[INFO] User Feedback: " + feedback);
        AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Feedback Received", "Thanks for your feedback!");
    }

    @FXML private void onHomeClick(ActionEvent actionEvent) {}
    @FXML private void onHistoryClick(ActionEvent actionEvent) {
        switchScene("/views/customer/CompletedOrders.fxml");
    }
    @FXML private void onOrderClick(ActionEvent actionEvent) {}
    @FXML private void onFeedbackClick(ActionEvent actionEvent) {}
}