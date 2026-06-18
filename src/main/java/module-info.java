module com.videolibrary {
    requires javafx.controls;
    requires javafx.fxml;

    // Add these two lines to grant access to RMI and JDBC Database tools
    requires java.rmi;
    requires java.sql;

    opens com.videolibrary to javafx.fxml;
    opens com.videolibrary.models to java.rmi; // Allows RMI to serialize your Movie model data

    exports com.videolibrary;
    exports com.videolibrary.rmi;
    exports com.videolibrary.models;
}