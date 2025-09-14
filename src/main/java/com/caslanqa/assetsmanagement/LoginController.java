package com.caslanqa.assetsmanagement;

import com.caslanqa.utils.DbHelper;
import com.caslanqa.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    void initialize() {
        DbHelper.initializeDatabase();
    }

    @FXML
    private void onLoginButtonClick(ActionEvent event) throws SQLException {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (DbHelper.authenticateUser(user, pass)) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/caslanqa/assetsmanagement/appPanel.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                FormUtils.showSystemError("Warning", "Failed to load main UI.");
            }
        }
    }

    public void signUpBtn(ActionEvent event)  {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/caslanqa/assetsmanagement/signUpPanel.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            FormUtils.showSystemError("Warning", "Failed to load main UI.");
        }

    }
}


