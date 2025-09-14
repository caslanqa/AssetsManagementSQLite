module com.caslanqa.assetsmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.sql;
    requires javafx.graphics;
    requires org.yaml.snakeyaml;
    requires jbcrypt;
    requires javafx.base;

    opens com.caslanqa.assetsmanagement to javafx.fxml;
    exports com.caslanqa.assetsmanagement;
}