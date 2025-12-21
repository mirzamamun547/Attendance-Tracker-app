package com.example.myapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        userTypeLabel.setText(type + " Login");
    }

    @FXML
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all fields!");
            return;
        }

        boolean success = UserDAO.login(email, password, userType);

        if (success) {
            showAlert("Success", "Login Successful!");

            try {
                FXMLLoader loader;
                if ("Student".equalsIgnoreCase(userType)) {

                    loader = new FXMLLoader(getClass().getResource("student.fxml"));
                } else {

                    loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
                }

                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            showAlert("Error", "Invalid email or password!");
        }
    }

    @FXML
    public void back(ActionEvent event) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("hello-view.fxml"));

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void goToSignup(ActionEvent event) {
        if (!"Teacher".equals(userType)) {
            showAlert("Error", "Students cannot signup!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
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
