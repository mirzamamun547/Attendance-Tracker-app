package com.example.myapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class attendanceController {

    @FXML
    private ComboBox<String> classBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<Student> tableView;
    @FXML
    private TableColumn<Student, String> rollCol;
    @FXML
    private TableColumn<Student, String> nameCol;
    @FXML
    private TableColumn<Student, Boolean> presentCol;
    @FXML
    private Button saveBtn;

    private final ObservableList<Student> students = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        classBox.getItems().addAll("Class 1", "Class 2", "Class 3");

        rollCol.setCellValueFactory(c -> c.getValue().rollNoProperty());
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());

        presentCol.setCellValueFactory(c -> c.getValue().presentProperty());
        presentCol.setCellFactory(CheckBoxTableCell.forTableColumn(presentCol));

        tableView.setEditable(true);
        tableView.setItems(students);

        classBox.setOnAction(e -> loadStudentsForClass(classBox.getValue()));
        saveBtn.setOnAction(e -> saveAttendance());
    }

    private void loadStudentsForClass(String className) {

        students.clear();

        String sql = "SELECT id, roll_no, name FROM students WHERE class=?";

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, className);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                            rs.getInt("id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            false
                    ));
                }
            }

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
            stage.setTitle("Dashboard");
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
    @FXML
    private void openReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/myapp/reports.fxml"));

            Parent root = loader.load();


            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("Reports");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAttendance() {

        LocalDate date = datePicker.getValue();
        String className = classBox.getValue();

        if (date == null || className == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Please select class and date").show();
            return;
        }

        String sql = """
                INSERT INTO attendance (student_id, date, present)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (Student s : students) {
                ps.setInt(1, s.getId());
                ps.setString(2, date.toString());
                ps.setInt(3, s.isPresent() ? 1 : 0);
                ps.addBatch();
            }

            ps.executeBatch();
            new Alert(Alert.AlertType.INFORMATION,
                    "Attendance saved successfully").show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

