package com.example.myapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class login {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label userTypeLabel;

    private String userType;


    public void setUserType(String type) {
        this.userType = type;
        if (userTypeLabel != null) {
            userTypeLabel.setText(type + " Login");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all fields!");
            return;
        }


        if (userType.equals("Student")) {
            if (email.equals("student@gmail.com") && password.equals("1234")) {
                showAlert("Success", "Student Login Successful!");
                //student
            } else {
                showAlert("Error", "Invalid email or password!");
            }
        } else if (userType.equals("Teacher")) {
            if (email.equals("teacher@gmail.com") && password.equals("1234")) {
                showAlert("Success", "Teacher Login Successful!");
                // teacher
            } else {
                showAlert("Error", "Invalid email or password!");
            }
        }
    }


    @FXML
    private void goToSignup(ActionEvent event) {
        if ("Teacher".equals(userType)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Students cannot signup!");
        }
    }


    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }
}
