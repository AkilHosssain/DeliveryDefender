package app.models;

import javafx.beans.property.SimpleStringProperty;

public class Order {
    private final SimpleStringProperty orderNumber;
    private final SimpleStringProperty customerName;
    private final SimpleStringProperty address;
    private final SimpleStringProperty phoneNumber;
    private final SimpleStringProperty deliveryDate;
    private final SimpleStringProperty timeFrame;
    private final SimpleStringProperty status;
    private final SimpleStringProperty driverName;

    public Order(String orderNumber, String customerName, String address, String phoneNumber,
                 String deliveryDate, String timeFrame, String status, String driverName) {
        this.orderNumber = new SimpleStringProperty(orderNumber);
        this.customerName = new SimpleStringProperty(customerName);
        this.address = new SimpleStringProperty(address);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.deliveryDate = new SimpleStringProperty(deliveryDate);
        this.timeFrame = new SimpleStringProperty(timeFrame);
        this.status = new SimpleStringProperty(status);
        this.driverName = new SimpleStringProperty(driverName);
    }

    public SimpleStringProperty orderNumberProperty() { return orderNumber; }
    public SimpleStringProperty customerNameProperty() { return customerName; }
    public SimpleStringProperty addressProperty() { return address; }
    public SimpleStringProperty phoneNumberProperty() { return phoneNumber; }
    public SimpleStringProperty deliveryDateProperty() { return deliveryDate; }
    public SimpleStringProperty timeFrameProperty() { return timeFrame; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty driverNameProperty() { return driverName; }
}