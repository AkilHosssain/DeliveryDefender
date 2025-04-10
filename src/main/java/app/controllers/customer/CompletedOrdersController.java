package app.controllers.customer;

import app.models.Order;
import app.utils.AlertUtil;
import app.utils.switchToScene;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CompletedOrdersController {

    @FXML private Button backButton;
    @FXML private TableView<Order> completedOrdersTable;
    @FXML private TableColumn<Order, String> orderNumberColumn;
    @FXML private TableColumn<Order, String> customerNameColumn;
    @FXML private TableColumn<Order, String> addressColumn;
    @FXML private TableColumn<Order, String> phoneNumberColumn;
    @FXML private TableColumn<Order, String> deliveryDateColumn;
    @FXML private TableColumn<Order, String> timeFrameColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> driverNameColumn;

    private static final String DATABASE_URL = "jdbc:sqlite:database/DeliveryDefender.db";

    /** Initialize the TableView when the page loads */
    @FXML
    private void initialize() {
        orderNumberColumn.setCellValueFactory(cellData -> cellData.getValue().orderNumberProperty());
        customerNameColumn.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        phoneNumberColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
        deliveryDateColumn.setCellValueFactory(cellData -> cellData.getValue().deliveryDateProperty());
        timeFrameColumn.setCellValueFactory(cellData -> cellData.getValue().timeFrameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        driverNameColumn.setCellValueFactory(cellData -> cellData.getValue().driverNameProperty());

        loadCompletedOrders();
    }

    /** Fetch completed orders from the database */
    private void loadCompletedOrders() {
        Task<ObservableList<Order>> loadDataTask = new Task<>() {
            @Override
            protected ObservableList<Order> call() throws Exception {
                String query = """
                        SELECT order_number, customer_name, address, phone_number, delivery_date, time_frame, status, driver
                        FROM orderlist
                        WHERE status = 'Completed'
                        """;

                ObservableList<Order> tempOrderList = FXCollections.observableArrayList();

                try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                     PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        tempOrderList.add(new Order(
                                rs.getString("order_number"),
                                rs.getString("customer_name"),
                                rs.getString("address"),
                                rs.getString("phone_number"),
                                rs.getString("delivery_date"),
                                rs.getString("time_frame"),
                                rs.getString("status"),
                                rs.getString("driver")
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to load completed orders: " + e.getMessage());
                    Platform.runLater(() -> AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to fetch completed orders."));
                }

                return tempOrderList;
            }
        };

        loadDataTask.setOnSucceeded(event -> completedOrdersTable.setItems(loadDataTask.getValue()));
        new Thread(loadDataTask).start();
    }
    public void onBackClick(ActionEvent actionEvent) {
        switchToScene.apply(backButton.getScene(), "/views/customer/CustomerDashboard.fxml");
    }
}