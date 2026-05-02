package com.example.myapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StudentController {



        @FXML private TextField studentIdField;
        @FXML private TextField studentNameField;
        @FXML private TextField studentCourseField;
        @FXML private ComboBox<String> classBox;

        @FXML private TableView<AttendanceRecord> attendanceTable;
        @FXML private TableColumn<AttendanceRecord, String> dateColumn;
        @FXML private TableColumn<AttendanceRecord, String> statusColumn;
        @FXML private TableColumn<AttendanceRecord, String> remarksColumn;

        @FXML private DatePicker leaveDatePicker;
        @FXML private TextArea absenceReasonArea;
        @FXML private Button submitReasonButton;
        @FXML private Button refreshButton;
        @FXML private Button exportButton;
    @FXML private Label attendancePercentLabel;
    @FXML private PieChart attendancePieChart;


    private int studentId;
        private final ObservableList<AttendanceRecord> attendanceList = FXCollections.observableArrayList();
        private final ObservableList<String> classNames = FXCollections.observableArrayList();
        private final Map<String, Integer> classMap = new HashMap<>();

        public void setStudentId(int studentId) {
            this.studentId = studentId;
            loadStudentInfo();
            loadStudentClasses();
        }

        @FXML
        public void initialize() {
            dateColumn.setCellValueFactory(c -> c.getValue().dateProperty());
            statusColumn.setCellValueFactory(c -> c.getValue().statusProperty());
            remarksColumn.setCellValueFactory(c -> c.getValue().remarksProperty());

            attendanceTable.setItems(attendanceList);
            attendancePieChart.setTitle("Attendance Overview");
            attendancePieChart.setLabelsVisible(true);

            submitReasonButton.setOnAction(this::submitReason);
            refreshButton.setOnAction(e -> {
                String selectedClass = classBox.getValue();
                if (selectedClass != null) loadAttendanceForClass(selectedClass);
            });
            exportButton.setOnAction(e -> exportAttendance());
        }

        private void loadStudentInfo() {

            String sql = "SELECT roll_no, name FROM students WHERE id=?";
            try (Connection con = DButil.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    studentIdField.setText(rs.getString("roll_no"));
                    studentNameField.setText(rs.getString("name"));
                    studentCourseField.setText("");
                    System.out.println("Loaded student: Roll=" + rs.getString("roll_no") + ", Name=" + rs.getString("name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loadStudentClasses() {
            classNames.clear();
            classMap.clear();

            String sql = "SELECT c.id, c.class_name " +
                    "FROM classes c " +
                    "JOIN student_classes sc ON c.id = sc.class_id " +
                    "WHERE sc.student_id=?";

            try (Connection con = DButil.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int classId = rs.getInt("id");
                    String className = rs.getString("class_name");
                    classNames.add(className);
                    classMap.put(className, classId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            classBox.setItems(classNames);
            if (!classNames.isEmpty()) {
                classBox.setValue(classNames.get(0));
                studentCourseField.setText(classNames.get(0));
                loadAttendanceForClass(classNames.get(0));
            }

            classBox.setOnAction(e -> {
                String selectedClass = classBox.getValue();
                if (selectedClass != null) {
                    studentCourseField.setText(selectedClass);
                    loadAttendanceForClass(selectedClass);
                }
            });
        }
    private void loadAttendanceForClass(String className) {
        attendanceList.clear();
        Integer classId = classMap.get(className);
        if (classId == null) return;

        String sql = "SELECT date, present, remarks " +
                "FROM attendance " +
                "WHERE student_id=? AND class_id=? " +
                "ORDER BY date DESC";

        int totalDays = 0;
        int presentDays = 0;

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            ResultSet rs = ps.executeQuery();

            boolean hasUnexplainedAbsence = false;

            while (rs.next()) {
                String date = rs.getString("date");
                boolean present = rs.getInt("present") == 1;
                String status = present ? "Present" : "Absent";
                String remarks = rs.getString("remarks") != null ? rs.getString("remarks") : "";

                attendanceList.add(new AttendanceRecord(date, status, remarks));

                totalDays++;
                if (present) presentDays++;

                if (!present && remarks.isBlank()) {
                    hasUnexplainedAbsence = true;
                }
            }

            // absenceReasonArea.setDisable(!hasUnexplainedAbsence);
            // submitReasonButton.setDisable(!hasUnexplainedAbsence);

            // ✅ Calculate percentage
            double percent = totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0;
            attendancePercentLabel.setText(
                    String.format("Attendance: %.1f%%", percent)
            );

            attendancePercentLabel.setStyle(
                    percent < 70
                            ? "-fx-text-fill: red; -fx-font-weight: bold;"
                            : "-fx-text-fill: green; -fx-font-weight: bold;"
            );


            updatePieChart(presentDays, totalDays - presentDays);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePieChart(int presentDays, int absentDays) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Present", presentDays),
                new PieChart.Data("Absent", absentDays)
        );

        attendancePieChart.setData(pieData);


        pieData.get(0).getNode().setStyle("-fx-pie-color: #2ecc71;");
        pieData.get(1).getNode().setStyle("-fx-pie-color: #e74c3c;");
    }

    private void submitReason(ActionEvent event) {
        String reason = absenceReasonArea.getText().trim();
        LocalDate leaveDate = leaveDatePicker.getValue();

        if (reason.isEmpty() || leaveDate == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a date and enter a reason").show();
            return;
        }

        String selectedClass = classBox.getValue();
        Integer classId = classMap.get(selectedClass);
        if (classId == null) {
            new Alert(Alert.AlertType.WARNING, "Invalid class selection").show();
            return;
        }

        String sql = "INSERT INTO leave_requests (student_id, class_id, date, reason, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            ps.setString(3, leaveDate.toString());
            ps.setString(4, reason);

            int inserted = ps.executeUpdate();
            if (inserted > 0) {
                new Alert(Alert.AlertType.INFORMATION, "Leave Request submitted successfully!").show();
                absenceReasonArea.clear();
                leaveDatePicker.setValue(null);
            } else {
                new Alert(Alert.AlertType.WARNING, "Failed to submit leave request.").show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        private void exportAttendance() {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Attendance");
            fileChooser.setInitialFileName("attendance.csv");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            java.io.File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("Date,Status,Remarks\n");
                    for (AttendanceRecord r : attendanceList) {
                        writer.write(r.getDate() + "," + r.getStatus() + "," + r.getRemarks() + "\n");
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Export successful").show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


