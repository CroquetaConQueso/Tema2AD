module OptativaTema2AccesoDatos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens application.ui to javafx.fxml;
    opens application.model to javafx.base;

    exports application;
    exports application.ui;
}
