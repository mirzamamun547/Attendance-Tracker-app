package com.example.myapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

        @FXML private TextArea absenceReasonArea;
        @FXML private Button submitReasonButton;
        @FXML private Button refreshButton;
        @FXML private Button exportButton;

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

            submitReasonButton.setOnAction(this::submitReason);
            refreshButton.setOnAction(e -> {
                String selectedClass = classBox.getValue();
                if (selectedClass != null) loadAttendanceForClass(selectedClass);
            });
            exportButton.setOnAction(e -> exportAttendance());
        }

        private void loadStudentInfo() {
            // ✅ only fetch roll_no and name from students
            String sql = "SELECT roll_no, name FROM students WHERE id=?";
            try (Connection con = DButil.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    studentIdField.setText(rs.getString("roll_no"));
                    studentNameField.setText(rs.getString("name"));
                    // course info will be filled from classes join
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

            // ✅ join through student_classes
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
                studentCourseField.setText(classNames.get(0)); // ✅ show first class as course
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

                    if (!present && (remarks.isEmpty() || remarks.isBlank())) {
                        hasUnexplainedAbsence = true;
                    }
                }

                absenceReasonArea.setDisable(!hasUnexplainedAbsence);
                submitReasonButton.setDisable(!hasUnexplainedAbsence);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void submitReason(ActionEvent event) {
            String reason = absenceReasonArea.getText().trim();
            if (reason.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please enter a reason").show();
                return;
            }

            AttendanceRecord selectedRecord = attendanceTable.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                new Alert(Alert.AlertType.WARNING, "Please select an absence record from the table").show();
                return;
            }

            if ("Present".equalsIgnoreCase(selectedRecord.getStatus())) {
                new Alert(Alert.AlertType.WARNING, "You can only submit a reason for absences").show();
                return;
            }

            String selectedClass = classBox.getValue();
            Integer classId = classMap.get(selectedClass);
            if (classId == null) {
                new Alert(Alert.AlertType.WARNING, "Invalid class selection").show();
                return;
            }

            String sql = "UPDATE attendance SET remarks=? " +
                    "WHERE student_id=? AND class_id=? AND date=? AND present=0";

            try (Connection con = DButil.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, reason);
                ps.setInt(2, studentId);
                ps.setInt(3, classId);
                ps.setString(4, selectedRecord.getDate());

                int updated = ps.executeUpdate();
                if (updated > 0) {
                    new Alert(Alert.AlertType.INFORMATION, "Reason submitted successfully").show();
                    absenceReasonArea.clear();
                    loadAttendanceForClass(selectedClass);
                } else {
                    new Alert(Alert.AlertType.WARNING, "No matching absence record found").show();
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


