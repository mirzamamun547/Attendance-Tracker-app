package com.example.myapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class reportsController {
    @FXML
    private void openAttendance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/myapp/attendance.fxml"));

            Parent root = loader.load();


            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("Take Attendance");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/myapp/Dashboard.fxml"));

            Parent root = loader.load();


            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("Take Attendance");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/myapp/hello-view.fxml"));

            Parent root = loader.load();


            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("LOG OUT");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
