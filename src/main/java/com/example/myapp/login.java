package com.example.myapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class login {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label userTypeLabel;

    private String userType;

    public void setUserType(String type) {
        this.userType = type;
        userTypeLabel.setText(type + " Login");
    }
    @FXML
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all fields!");
            return;
        }

        Integer userId = UserDAO.login(email, password, userType);

        if (userId != null) {
            Session.set(userId, userType); // save session
            showAlert("Success", "Login Successful!");

            try {
                FXMLLoader loader;
                Parent root;
                Scene scene;
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                if ("Student".equalsIgnoreCase(userType)) {
                    loader = new FXMLLoader(getClass().getResource("/com/example/myapp/student.fxml"));
                    root = loader.load();

                    // ✅ get controller and pass studentId
                    StudentController controller = loader.getController();
                    controller.setStudentId(userId);
                    System.out.println("Logged-in student ID: " + userId);

                    scene = new Scene(root);
                } else {
                    loader = new FXMLLoader(getClass().getResource("/com/example/myapp/Dashboard.fxml"));
                    root = loader.load();

                    // ✅ get controller and pass teacherId
                    DashboardController controller = loader.getController();
                    controller.setCurrentTeacherId(userId);
                    System.out.println("Logged-in teacher ID: " + userId);

                    scene = new Scene(root);
                }

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
    private void back(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
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
        if (!"Teacher".equalsIgnoreCase(userType)) {
            showAlert("Error", "Students cannot signup!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signup.fxml"));
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
