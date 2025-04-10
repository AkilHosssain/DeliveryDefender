package app.controllers.driver;

import app.utils.SessionManager;
import app.utils.AlertUtil;
import app.utils.switchToScene;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;

public class DriverDashboardController implements Initializable {

    @FXML
    private VBox tasksContainer;

    @FXML
    private Menu riderInfoMenu;

    @FXML
    private ImageView barcodeImageView;

    private static final String DB_URL = "jdbc:sqlite:database/DeliveryDefender.db";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRiderDetails();
        loadTasksForRider();
    }

    private void loadRiderDetails() {
        String driverEmail = SessionManager.getUsername();
        String query = "SELECT name, id FROM users WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, driverEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String riderName = rs.getString("name");
                    String userID = rs.getString("id");
                    riderInfoMenu.setText(riderName + " (ID: " + userID + ")");
                } else {
                    riderInfoMenu.setText("No Rider Found");
                }
            }
        } catch (Exception e) {
            riderInfoMenu.setText("Error Loading Rider Info");
        }
    }

    private void loadTasksForRider() {
        tasksContainer.getChildren().clear(); // Clear existing tasks

        String riderEmail = SessionManager.getUsername();
        String query = "SELECT name FROM users WHERE email = ?";
        String sql = "SELECT order_number, customer_id, customer_name, address, delivery_date, time_frame, description FROM orderlist WHERE driver = ? AND status = 'In Progress'";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, riderEmail);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String riderName = rs.getString("name");

                    try (PreparedStatement taskStmt = conn.prepareStatement(sql)) {
                        taskStmt.setString(1, riderName);
                        ResultSet taskRs = taskStmt.executeQuery();

                        boolean hasTasks = false;

                        while (taskRs.next()) {
                            hasTasks = true;

                            int orderNumber = taskRs.getInt("order_number");
                            String customerName = taskRs.getString("customer_name");
                            String address = taskRs.getString("address");
                            String deliveryDate = taskRs.getString("delivery_date");
                            String deliveryTime = taskRs.getString("time_frame");
                            String customerNote = taskRs.getString("description");

                            HBox orderRow = new HBox(10);
                            Label orderInfo = new Label("Order #" + orderNumber + " - " + customerName + " - " + address + " - " + deliveryDate + " " + deliveryTime + " - Note: " + customerNote);
                            Button deliverButton = new Button("Scan Barcode");
                            deliverButton.setOnAction(event -> uploadBarcode(orderNumber, customerName, address));

                            orderRow.getChildren().addAll(orderInfo, deliverButton);
                            tasksContainer.getChildren().add(orderRow);
                        }

                        if (!hasTasks) {
                            Label noTasksLabel = new Label("✅ No order to deliver right now.");
                            tasksContainer.getChildren().add(noTasksLabel);
                        }
                    }
                } else {
                    AlertUtil.showAlert(Alert.AlertType.ERROR, "No Rider Found", "Could not find rider with email: " + riderEmail);
                }
            }
        } catch (SQLException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Error occurred while loading rider tasks.");
        }
    }


    private void uploadBarcode(int orderNumber, String customerName, String address) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (barcodeImageView != null) {  // ✅ Check if barcodeImageView is initialized
                barcodeImageView.setImage(new Image(selectedFile.toURI().toString()));
            } else {
                System.out.println("Error: barcodeImageView is NULL!");
            }
            scanBarcodeImage(selectedFile, orderNumber, customerName, address);
        }
    }


    private void scanBarcodeImage(File barcodeImageFile, int orderNumber,  String customerName, String address) {
        try {
            BufferedImage bufferedImage = ImageIO.read(barcodeImageFile);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(bufferedImage)));

            Result result = new MultiFormatReader().decode(bitmap);
            String scannedData = result.getText();

            boolean isMatch = scannedData.contains("Order Number: " + orderNumber) &&
                    scannedData.contains("Customer: " + customerName) &&
                    scannedData.contains("Address: " + address);

            if (isMatch) {
                showAlert(Alert.AlertType.INFORMATION, "Verification Success", "🎉You are at the Correct Location!");
                String updateQuery= "UPDATE orderlist SET status = 'Completed' WHERE order_number = ?";
                try (Connection conn = DriverManager.getConnection(DB_URL)) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                        stmt.setInt(1, orderNumber);
                        stmt.executeUpdate();
                    }
                }
                tasksContainer.getChildren().clear();  // Clear old entries
                loadTasksForRider();                   // Load updated entries
            } else {
                showAlert(Alert.AlertType.ERROR, "Verification Failed", "❌ You are at the wrong location.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Scan Error", "Failed to read barcode.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    public void onProfileClick(ActionEvent actionEvent) {
    }
    public void onMapSettingsClick(ActionEvent actionEvent) {}
    public void onVehicleSettingsClick(ActionEvent actionEvent) {}
    public void onDeleteAccountClick(ActionEvent actionEvent) {}
    public void onLogoutClick(ActionEvent actionEvent) {


            // Clear user session data
            SessionManager.clearSession();

            Scene currentScene = tasksContainer.getScene();
            switchToScene.apply(currentScene, "/views/login.fxml");

    }
}
