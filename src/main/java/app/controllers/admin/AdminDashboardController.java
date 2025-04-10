package app.controllers.admin;

import app.models.Task;
import app.utils.AlertUtil;
import app.utils.SessionManager;
import app.utils.switchToScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.sql.*;

public class AdminDashboardController {


    @FXML
    private TableView<Task> pendingTasksTable, inProgressTasksTable;

    @FXML
    private TableColumn<Task, Integer> orderNumber, orderNumberProgress;

    @FXML
    private TableColumn<Task, String> customerName, customerNameProgress,
            deliveryDate, deliveryDateProgress, status, statusProgress,
            address, addressProgress, note, noteProgress;

    @FXML
    private StackPane rootPane;

    private static final String DB_URL = "jdbc:sqlite:database/DeliveryDefender.db";

    @FXML
    public void initialize() {
        setupTableColumns();
        loadTasks("Pending", pendingTasksTable);
        loadTasks("In Progress", inProgressTasksTable);
    }

    private void setupTableColumns() {
        orderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        customerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        deliveryDate.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        note.setCellValueFactory(new PropertyValueFactory<>("description"));

        orderNumberProgress.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        customerNameProgress.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        deliveryDateProgress.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        statusProgress.setCellValueFactory(new PropertyValueFactory<>("status"));
        addressProgress.setCellValueFactory(new PropertyValueFactory<>("address"));
        noteProgress.setCellValueFactory(new PropertyValueFactory<>("description"));

        pendingTasksTable.setPlaceholder(new Label("No pending orders."));
        inProgressTasksTable.setPlaceholder(new Label("No in-progress orders."));
    }

    private void loadTasks(String taskStatus, TableView<Task> tableView) {
        ObservableList<Task> tasksList = FXCollections.observableArrayList();
        String sql = "SELECT order_number, customer_name, address, phone_number, " +
                "delivery_date, time_frame, description, driver, status " +
                "FROM orderlist WHERE status = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, taskStatus);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getString("customer_name") != null && !rs.getString("customer_name").isEmpty()) {
                    tasksList.add(new Task(
                            rs.getInt("order_number"),
                            rs.getString("customer_name"),
                            rs.getString("address"),
                            rs.getString("phone_number"),
                            rs.getString("delivery_date"),
                            rs.getString("time_frame"),
                            rs.getString("description"),
                            rs.getString("driver"),
                            rs.getString("status")
                    ));
                }
            }
            tableView.setItems(tasksList);
        } catch (SQLException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load " + taskStatus + " tasks.");
        }
    }


    @FXML
    private void onHomeClick() {
        switchScene("/views/admin/AdminDashboard.fxml", "Error loading Home page");
    }

    @FXML
    private void onProfileClick() {
        switchScene("/views/admin/Profile.fxml", "Profile page to view profile");
    }

    @FXML
    private void onPendingTaskClick() {
        switchScene("/views/admin/pendingTasks.fxml", "Pending Tasks");
    }

    @FXML
    private void onInProgressTaskClick() {
        switchScene("/views/admin/inProgressPage.fxml", "In Progress Tasks");
    }

    @FXML
    private void onListOfDriversClick() {
        System.out.println("List of Drivers clicked");
    }

    @FXML
    private void onLogoutClick() {
        // Clear user session data
        SessionManager.clearSession();

        Scene currentScene = rootPane.getScene();
        switchToScene.apply(currentScene, "/views/login.fxml");
    }

    protected void switchScene(String fxmlPath, String errorMessage) {
        try {
            Scene currentScene = rootPane.getScene();
            switchToScene.apply(currentScene, fxmlPath);
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", errorMessage + " could not be found.");
        }
    }
}