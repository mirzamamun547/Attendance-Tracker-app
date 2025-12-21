package com.example.myapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class signup {

    @FXML
    private TextField nameField, emailField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private void handleSignup(ActionEvent event) {

        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String cpass = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || cpass.isEmpty()) {
            showAlert("Error", "Please fill all fields!");
            return;
        }

        if (!pass.equals(cpass)) {
            showAlert("Error", "Passwords do not match!");
            return;
        }

        boolean success = UserDAO.register(name, email, pass, "Teacher");

        if (success) {
            showAlert("Success", "Account created!");
            goToLogin(null);
        } else {
            showAlert("Error", "Email already exists!");
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }
}
