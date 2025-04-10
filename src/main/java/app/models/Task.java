package app.models;

public class Task {
    private final int orderNumber;
    private final String customerName;
    private final String address;
    private final String phoneNumber;
    private final String deliveryDate;
    private final String time;
    private final String description;
    private String driver;
    private final String status;

    public Task(int orderNumber, String customerName, String address, String phoneNumber,
                String deliveryDate, String time, String description, String driver, String status) {
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.deliveryDate = deliveryDate;
        this.time = time;
        this.description = description;
        this.driver = driver;
        this.status = status;
    }

    public int getOrderNumber() { return orderNumber; }
    public String getCustomerName() { return customerName; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; } //
    public String getDeliveryDate() { return deliveryDate; }
    public String getTime() { return time; }
    public String getDescription() { return description; }
    public String getDriver() { return driver; }
    public String getStatus() { return status; }

    //  Setter for driver updates
    public void setDriver(String driver) { this.driver = driver; }
}