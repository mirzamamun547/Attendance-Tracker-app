package com.example.myapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

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


        rollCol.setCellValueFactory(cell -> cell.getValue().rollNoProperty());
        nameCol.setCellValueFactory(cell -> cell.getValue().nameProperty());


        presentCol.setCellValueFactory(cell -> cell.getValue().presentProperty());
        presentCol.setCellFactory(CheckBoxTableCell.forTableColumn(presentCol));
        presentCol.setEditable(true);


        tableView.setEditable(true);


        tableView.setItems(students);


        classBox.setOnAction(e -> loadStudentsForClass(classBox.getValue()));


        saveBtn.setOnAction(e -> saveAttendance());
    }

    private void loadStudentsForClass(String className) {
        students.clear();


        if ("Class 1".equals(className)) {
            students.addAll(
                    new Student("101", "Alice", false),
                    new Student("102", "Bob", false),
                    new Student("103", "Charlie", false)
            );
        } else if ("Class 2".equals(className)) {
            students.addAll(
                    new Student("201", "David", false),
                    new Student("202", "Eve", false)
            );
        } else {
            students.addAll(
                    new Student("301", "Frank", false)
            );
        }
    }


    private void saveAttendance() {
        LocalDate date = datePicker.getValue();
        String className = classBox.getValue();

        if (className == null || date == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select class and date.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("Attendance for ").append(className).append(" on ").append(date).append(":\n");

        for (Student student : students) {
            report.append(student.getRollNo())
                    .append(" - ")
                    .append(student.getName())
                    .append(" : ")
                    .append(student.isPresent() ? "Present" : "Absent")
                    .append("\n");
        }


        Alert alert = new Alert(Alert.AlertType.INFORMATION, report.toString(), ButtonType.OK);
        alert.setHeaderText("Attendance Saved");
        alert.showAndWait();
    }
}
