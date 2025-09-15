module com.caslanqa.assetsmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires org.yaml.snakeyaml;
    requires jbcrypt;
    requires javafx.base;
    requires com.google.gson;
    requires java.net.http;

    opens com.caslanqa.assetsmanagement to javafx.fxml;
    exports com.caslanqa.assetsmanagement;
}