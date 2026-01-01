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
import java.util.HashMap;
import java.util.Map;
public class attendanceController {

    @FXML private ComboBox<String> classBox;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Student> tableView;
    @FXML private TableColumn<Student, String> rollCol;
    @FXML private TableColumn<Student, String> nameCol;
    @FXML private TableColumn<Student, Boolean> presentCol;
    @FXML private Button saveBtn;

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<String> classNames = FXCollections.observableArrayList();
    private final Map<String, Integer> classMap = new HashMap<>();
    private int currentTeacherId;

    @FXML
    public void initialize() {
        rollCol.setCellValueFactory(c -> c.getValue().rollNoProperty());
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        presentCol.setCellValueFactory(c -> c.getValue().presentProperty());
        presentCol.setCellFactory(CheckBoxTableCell.forTableColumn(presentCol));

        tableView.setEditable(true);
        tableView.setItems(students);

        classBox.setItems(classNames);
        classBox.setOnAction(e -> {
            String selected = classBox.getValue();
            if (selected != null) loadStudentsForClass(selected);
        });

        saveBtn.setOnAction(e -> saveAttendance());
    }

    public void setCurrentTeacherId(int teacherId) {
        this.currentTeacherId = teacherId;
        loadClasses();
    }

    private void loadClasses() {
        classNames.clear();
        classMap.clear();
        String sql = "SELECT id, class_name FROM classes WHERE teacher_id=?";
        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentTeacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("class_name");
                int id = rs.getInt("id");
                classNames.add(name);
                classMap.put(name, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStudentsForClass(String className) {
        students.clear();
        Integer classId = classMap.get(className);
        if (classId == null) return;

        // ✅ FIX: filter by class’s teacher_id, not student’s
        String sql = "SELECT s.id, s.roll_no, s.name " +
                "FROM students s " +
                "JOIN student_classes sc ON s.id = sc.student_id " +
                "JOIN classes c ON sc.class_id = c.id " +
                "WHERE sc.class_id=? AND c.teacher_id=?";

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, currentTeacherId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("id"),
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        false,
                        className
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAttendance() {
        LocalDate date = datePicker.getValue();
        String className = classBox.getValue();
        if (date == null || className == null) {
            new Alert(Alert.AlertType.WARNING, "Please select class and date").show();
            return;
        }

        Integer classId = classMap.get(className);
        if (classId == null) {
            new Alert(Alert.AlertType.WARNING, "Invalid class selection").show();
            return;
        }

        String sql = "INSERT INTO attendance (student_id, class_id, date, present) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(student_id, class_id, date) DO UPDATE SET present=?";

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (Student s : students) {
                ps.setInt(1, s.getId());
                ps.setInt(2, classId);
                ps.setString(3, date.toString());
                ps.setInt(4, s.isPresent() ? 1 : 0);
                ps.setInt(5, s.isPresent() ? 1 : 0);
                ps.addBatch();
            }

            ps.executeBatch();
            new Alert(Alert.AlertType.INFORMATION, "Attendance saved successfully").show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Navigation
    @FXML
    private void openDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/myapp/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setCurrentTeacherId(currentTeacherId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/myapp/hello-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/myapp/reports.fxml"));
            Parent root = loader.load();
            reportsController controller = loader.getController();
            controller.setCurrentTeacherId(currentTeacherId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reports");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
