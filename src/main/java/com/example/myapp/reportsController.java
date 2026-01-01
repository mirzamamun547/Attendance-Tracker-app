package com.example.myapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class reportsController {

    @FXML private ComboBox<String> classFilter;
    @FXML private DatePicker dateFilter;
    @FXML private VBox reportCards;

    private final ObservableList<String> classes = FXCollections.observableArrayList();
    private final Map<String, Integer> classMap = new HashMap<>();

    private int currentTeacherId;

    // Called when teacherId is passed in from Dashboard
    public void setCurrentTeacherId(int teacherId) {
        this.currentTeacherId = teacherId;
        loadClasses();
        classFilter.setItems(classes); // populate combo box
    }

    private void loadClasses() {
        classes.clear();
        classMap.clear();
        String sql = "SELECT id, class_name FROM classes WHERE teacher_id=?";
        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentTeacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("class_name");
                int id = rs.getInt("id");
                classes.add(name);
                classMap.put(name, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        dateFilter.setValue(LocalDate.now());
    }

    @FXML
    private void filterReports(ActionEvent event) {
        String selectedClass = classFilter.getValue();
        LocalDate selectedDate = dateFilter.getValue();

        if (selectedClass == null || selectedDate == null) {
            new Alert(Alert.AlertType.WARNING, "Please select both class and date").show();
            return;
        }

        Integer classId = classMap.get(selectedClass);
        if (classId == null) {
            new Alert(Alert.AlertType.WARNING, "Invalid class selection").show();
            return;
        }

        loadReportsFromDB(classId, selectedDate.toString());
    }

    private void loadReportsFromDB(int classId, String date) {
        reportCards.getChildren().clear();
        String sql = "SELECT s.roll_no, s.name, a.present, a.remarks " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "WHERE a.class_id=? AND a.date=?";

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String roll = rs.getString("roll_no");
                String name = rs.getString("name");
                String status = rs.getInt("present") == 1 ? "Present" : "Absent";
                String remarks = rs.getString("remarks");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("reportCard.fxml"));
                Node card = loader.load();
                ReportCardController cardController = loader.getController();
                cardController.setData(roll, name, status, remarks);
                reportCards.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Navigation
    @FXML
    private void openDashboard(ActionEvent event) {
        switchScene(event, "/com/example/myapp/Dashboard.fxml", "Dashboard");
    }

    @FXML
    private void openAttendance(ActionEvent event) {
        switchScene(event, "/com/example/myapp/attendance.fxml", "Take Attendance");
    }

    @FXML
    private void logout(ActionEvent event) {
        switchScene(event, "/com/example/myapp/hello-view.fxml", "LOG OUT");
    }

    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController dash) {
                dash.setCurrentTeacherId(currentTeacherId);
            } else if (controller instanceof attendanceController att) {
                att.setCurrentTeacherId(currentTeacherId);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
