package app.controllers.admin;

import app.models.Task;
import app.utils.AlertUtil;
import app.utils.switchToScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PendingTasksController implements Initializable {

    @FXML
    private TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, Integer> orderNumber;

    @FXML
    private TableColumn<Task, String> customerName;

    @FXML
    private TableColumn<Task, String> address;

    @FXML
    private TableColumn<Task, String> phoneNumber;

    @FXML
    private TableColumn<Task, String> deliveryDate;

    @FXML
    private TableColumn<Task, String> time;

    @FXML
    private TableColumn<Task, String> description;

    @FXML
    private TableColumn<Task, String> driver;

    @FXML
    private TableColumn<Task, String> status;

    private static final String DB_URL = "jdbc:sqlite:database/DeliveryDefender.db";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        customerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        deliveryDate.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        driver.setCellFactory(column -> new TableCell<Task, String>() {
            private final Button assignButton = new Button("Assign Driver");

            {
                assignButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    showDriverSelectionPopup(task);
                });
            }

            @Override
            protected void updateItem(String driverName, boolean empty) {
                super.updateItem(driverName, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else if (driverName == null || driverName.trim().isEmpty() || driverName.equalsIgnoreCase("Not Assign")) {
                    setGraphic(assignButton);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(driverName);
                }
            }
        });

        loadPendingTasks();
    }

    private void loadPendingTasks() {
        ObservableList<Task> tasksList = FXCollections.observableArrayList();

        String sql = "SELECT order_number, customer_name, address, phone_number, " +
                "delivery_date, time_frame, description, driver, status " +
                "FROM orderlist WHERE status = 'Pending'";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("order_number"),
                        rs.getString("customer_name"),
                        rs.getString("address"),
                        rs.getString("phone_number"),
                        rs.getString("delivery_date"),
                        rs.getString("time_frame"),
                        rs.getString("description"),
                        rs.getString("driver"),
                        rs.getString("status")
                );
                tasksList.add(task);
            }

            tasksTable.setItems(tasksList);
            tasksTable.refresh();

        } catch (SQLException e) {
            Logger.getLogger(PendingTasksController.class.getName()).log(Level.SEVERE, null, e);
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load pending tasks.");
        }
    }

    private void showDriverSelectionPopup(Task task) {
        Stage stage = new Stage();
        VBox vbox = new VBox(10);
        ComboBox<String> driverComboBox = new ComboBox<>();
        Button assignButton = new Button("Assign Driver");

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE role = 'Driver'");
             ResultSet rs = stmt.executeQuery()) {

            driverComboBox.getItems().add("Not Assign");
            while (rs.next()) {
                driverComboBox.getItems().add(rs.getString("name"));
            }

        } catch (SQLException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch drivers.");
            return;
        }

        assignButton.setOnAction(event -> {
            String selectedDriver = driverComboBox.getValue();
            if (selectedDriver == null || selectedDriver.isEmpty()) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please choose a driver.");
            } else {
                updateDriverInDatabase(task, selectedDriver);
                stage.close();
            }
        });

        vbox.getChildren().addAll(driverComboBox, assignButton);
        stage.setScene(new Scene(vbox, 200, 100));
        stage.setTitle("Select Driver");
        stage.show();
    }

    private void updateDriverInDatabase(Task task, String driverName) {
        String updateSQL = "UPDATE orderlist SET driver = ?, status = 'In Progress' WHERE order_number = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setString(1, driverName);
            stmt.setInt(2, task.getOrderNumber());
            stmt.executeUpdate();

            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Driver assigned successfully!");

            task.setDriver(driverName);
            loadPendingTasks();

        } catch (SQLException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to assign driver.");
        }
    }

    public void onBackClick() {
        try {
            Scene currentScene = tasksTable.getScene();
            switchToScene.apply(currentScene, "/views/admin/AdminDashboard.fxml");
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "Error loading admin dashboard.");
        }
    }
}
