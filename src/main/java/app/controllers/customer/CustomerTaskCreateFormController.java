package app.controllers.customer;

import app.utils.AlertUtil;
import app.utils.switchToScene;
import app.utils.SessionManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class CustomerTaskCreateFormController {

    @FXML
    private TextField customerNameField;

    @FXML
    private TextArea addressArea;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private DatePicker deliveryDatePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private TextArea taskDescriptionArea;



    private static final String DB_URL = "jdbc:sqlite:database/DeliveryDefender.db";

    public void initialize() {
        // Ensure the database table is created
        createOrderListTable();
    }

    // Create the orderlist table if it doesn't exist
    private void createOrderListTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS orderlist (" +
                "order_number INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id INTERGER NOT NULL,"+
                "customer_name TEXT NOT NULL," +
                "address TEXT NOT NULL," +
                "phone_number TEXT NOT NULL," +
                "delivery_date TEXT NOT NULL," +
                "time_frame TEXT NOT NULL," +
                "description TEXT," +
                "status TEXT DEFAULT 'Pending'," +
                "driver TEXT DEFAULT 'Not Assign'"+
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Checked/Created the orderlist table.");

        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create/check the orderlist table.");
        }
    }



    @FXML
    public void onSaveOrderClick() {
        String customerName = customerNameField.getText();
        String address = addressArea.getText();
        String phoneNumber = phoneNumberField.getText();
        LocalDate deliveryDate = deliveryDatePicker.getValue();
        String startTime = startTimeField.getText();
        String endTime = endTimeField.getText();
        String description = taskDescriptionArea.getText();

        // Validate input fields
        if (customerName.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() ||
                deliveryDate == null || startTime.isEmpty() || endTime.isEmpty() ) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill all mandatory fields.");
            return;
        }

        String deliveryDateStr = deliveryDate.toString();
        String timeFrame = startTime + " to " + endTime;
        String customer_id= SessionManager.getId();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO orderlist (customer_id,customer_name, address, phone_number, delivery_date, time_frame, description, status, driver) " +
                             "VALUES (?, ?, ?, ?, ?, ?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer_id);
            stmt.setString(2, customerName);
            stmt.setString(3, address);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, deliveryDateStr);
            stmt.setString(6, timeFrame);
            stmt.setString(7, description);
            stmt.setString(8, "Pending");
            stmt.setString(9, "Not Assign");

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()){
                    if (generatedKeys.next()) {
                        int orderNumber = generatedKeys.getInt(1);
                        generateBarcode(customerName,address,orderNumber);
                    }
                }
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Order has been saved successfully.");
                clearForm();
            }
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save order. Please try again.");
        }
    }

    @FXML
    public void onCancelClick() {
        clearForm();
        System.out.println("Order creation cancelled.");
        try{
            Scene currentScene = customerNameField.getScene();
            switchToScene.apply(currentScene,"/views/customer/CustomerDashboard.fxml");
        }catch(Exception e){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "Not going to AdminDashboard");
        }
    }

    private void clearForm() {
        customerNameField.clear();
        addressArea.clear();
        phoneNumberField.clear();
        deliveryDatePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        taskDescriptionArea.clear();
    }


    //generate barcode using customer name and address
    private void generateBarcode(String customerName, String address, int order_number) {
        try{
            String barcodeContent = "Order Number: " +order_number+ "; Customer: " +customerName+ "; Address: " +address;
            BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeContent , BarcodeFormat.CODE_128, 300, 100);
            Path path = Paths.get("src/main/resources/images", order_number + ".barcode.png");
            MatrixToImageWriter.writeToPath(bitMatrix,"PNG",path);
            System.out.println("Barcode written to " + path.toString());

        }catch (Exception e){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create barcode.");
        }
    }
}
