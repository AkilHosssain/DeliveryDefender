module deliverydefender {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;

    opens app to javafx.fxml;
    exports app;
    exports app.controllers;
    opens app.controllers to javafx.fxml;
    exports app.controllers.admin;
    opens app.controllers.admin to javafx.fxml;
    exports app.controllers.customer;
    opens app.controllers.customer to javafx.fxml;
    exports app.controllers.driver;
    opens app.controllers.driver to javafx.fxml;
    opens app.models to javafx.base;
}