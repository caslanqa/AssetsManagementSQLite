package com.caslanqa.assetsmanagement;

import com.caslanqa.utils.DbHelper;
import com.caslanqa.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class RegistrationController {

    @FXML
    private Label messageLabel;

    @FXML
    private PasswordField passwordSignUp;

    @FXML
    private TextField usernameSignUp;

    @FXML
    private TextField currencyApiKey;

    @FXML
    void signUpBtn(MouseEvent event) throws SQLException {
        String user = usernameSignUp.getText();
        String pass = passwordSignUp.getText();
        String apiKey = currencyApiKey.getText();

        if (FormUtils.checkUsernamePassword(user,pass)){
            if (DbHelper.isUsernameExists(user))
                FormUtils.showError("Registration Error", "Username already exists.");

            DbHelper.registerUser(user, pass, apiKey);
            FormUtils.showInfo("Registration Successful", "User registered successfully.");

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/caslanqa/assetsmanagement/login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                messageLabel.setText("Failed to load main UI.");
                e.printStackTrace();
            }
        }

    }

}
