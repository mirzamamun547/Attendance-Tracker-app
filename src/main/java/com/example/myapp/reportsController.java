package com.example.myapp;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class reportsController {

    @FXML private ComboBox<String> classFilter;
    @FXML private DatePicker dateFilter;
    @FXML private VBox reportCards;

    private final ObservableList<String> classes = FXCollections.observableArrayList();
    private final Map<String, Integer> classMap = new HashMap<>();

    private int currentTeacherId;

    @FXML
    public void initialize() {
        dateFilter.setValue(LocalDate.now());
    }

    public void setCurrentTeacherId(int teacherId) {
        this.currentTeacherId = teacherId;
        loadClasses();
        classFilter.setItems(classes);
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
                String className = rs.getString("class_name");
                int classId = rs.getInt("id");
                classes.add(className);
                classMap.put(className, classId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void filterReports(ActionEvent event) {
        String className = classFilter.getValue();
        LocalDate date = dateFilter.getValue();

        if (className == null || date == null) {
            new Alert(Alert.AlertType.WARNING, "Select class and date").show();
            return;
        }

        Integer classId = classMap.get(className);
        if (classId == null) return;

        loadReportsFromDB(classId, date);
    }

    private void loadReportsFromDB(int classId, LocalDate date) {
        reportCards.getChildren().clear();

        String sql = """
        SELECT s.roll_no,
               s.name,
               a.present,
               a.remarks
        FROM attendance a
        JOIN students s ON a.student_id = s.id
        WHERE a.class_id = ?
        AND a.date = ?
        ORDER BY s.roll_no
        """;

        boolean hasData = false;

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.setString(2, date.toString());  // <-- fix here

            System.out.println("Querying classId=" + classId + " date=" + date);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String roll = rs.getString("roll_no");
                String name = rs.getString("name");
                String status = rs.getInt("present") == 1 ? "Present" : "Absent";
                String remarks = rs.getString("remarks");

                // ✅ Skip if remarks is null or blank
                if (remarks == null || remarks.isBlank()) continue;

                hasData = true;

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/myapp/reportCard.fxml"));
                    Node card = loader.load();

                    ReportCardController controller = loader.getController();
                    controller.setData(roll, name, status, remarks);

                    reportCards.getChildren().add(card);
                    System.out.println("Row -> Roll: " + roll +
                            ", Name: " + name +
                            ", Status: " + status +
                            ", Remarks: " + remarks);
                } catch (Exception e) {
                    System.err.println("Error loading report card: " + e.getMessage());
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @FXML
    private void openDashboard(ActionEvent event) {
        switchScene(event, "/com/example/myapp/Dashboard.fxml", "Dashboard");
    }

    @FXML
    private void openAttendance(ActionEvent event) {
        switchScene(event, "/com/example/myapp/attendance.fxml", "Attendance");
    }

    @FXML
    private void logout(ActionEvent event) {
        switchScene(event, "/com/example/myapp/hello-view.fxml", "Logout");
    }

    private void switchScene(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController d)
                d.setCurrentTeacherId(currentTeacherId);
            else if (controller instanceof attendanceController a)
                a.setCurrentTeacherId(currentTeacherId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
