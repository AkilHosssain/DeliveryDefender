package app.controllers.admin;

import app.utils.AlertUtil;
import app.utils.switchToScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import app.models.Task;

public class InProgressPageController implements Initializable {

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
        driver.setCellValueFactory(new PropertyValueFactory<>("driver"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadInProgressTasks();
    }

    private void loadInProgressTasks() {
        ObservableList<Task> tasksList = FXCollections.observableArrayList();

        String sql = "SELECT order_number, customer_name, address, phone_number, delivery_date, time_frame, description, driver, status FROM orderlist WHERE status = 'In Progress'";

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
            Logger.getLogger(InProgressPageController.class.getName()).log(Level.SEVERE, null, e);
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
