package com.example.myapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;



import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {


        @FXML
        private Button loginBtn, signupBtn;

        @FXML
        private HBox loginSignupBox;

        private boolean isTeacher = false;

        @FXML
        private void handleStudent(ActionEvent event) throws IOException {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            login loginController = fxmlLoader.getController();
            loginController.setUserType("Student");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        }

        @FXML
        private void handleTeacher() {
            isTeacher = true;
            loginSignupBox.setVisible(true);
            loginBtn.setVisible(true);
            signupBtn.setVisible(true);
        }

        @FXML
        private void handleLogin(ActionEvent event) throws IOException {
            if (isTeacher) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                Scene scene = new Scene(fxmlLoader.load());


                login loginController = fxmlLoader.getController();
                loginController.setUserType("Teacher");

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Login");
                stage.show();
            }
        }

        @FXML
        private void handleSignup(ActionEvent event) throws IOException {
            if (isTeacher) {

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("signup.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Signup");
                stage.show();
            }
        }
    }
