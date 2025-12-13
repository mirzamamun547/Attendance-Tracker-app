package com.example.myapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

import javafx.scene.Node;

public class DashboardController {

    @FXML
    private TableView<Student> studentTable;
    @FXML
    private TableColumn<Student, String> rollCol;
    @FXML
    private TableColumn<Student, String> nameCol;
    @FXML
    private TableColumn<Student, Boolean> presentCol;

    @FXML
    private Button addStudentBtn;
    @FXML
    private Button deleteStudentBtn;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    private final ObservableList<String> classes = FXCollections.observableArrayList("Class 1", "Class 2", "Class 3");

    @FXML
    public void initialize() {

        rollCol.setCellValueFactory(cell -> cell.getValue().rollNoProperty());
        nameCol.setCellValueFactory(cell -> cell.getValue().nameProperty());


        presentCol.setCellValueFactory(cell -> cell.getValue().presentProperty());
        presentCol.setCellFactory(CheckBoxTableCell.forTableColumn(presentCol));
        presentCol.setEditable(true);

        studentTable.setEditable(true);
        studentTable.setItems(studentList);


        addStudentBtn.setOnAction(e -> addStudent());
        deleteStudentBtn.setOnAction(e -> deleteStudent());
    }


    private void addStudent() {
        Dialog<Pair<String, String[]>> dialog = new Dialog<>();
        dialog.setTitle("Add Student");
        dialog.setHeaderText("Enter class, roll number, and name");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<String> classCombo = new ComboBox<>(classes);
        classCombo.setPromptText("Select Class");
        TextField rollField = new TextField();
        rollField.setPromptText("Roll No");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        grid.add(new Label("Class:"), 0, 0);
        grid.add(classCombo, 1, 0);
        grid.add(new Label("Roll No:"), 0, 1);
        grid.add(rollField, 1, 1);
        grid.add(new Label("Name:"), 0, 2);
        grid.add(nameField, 1, 2);

        dialog.getDialogPane().setContent(grid);


        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        classCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateInput(classCombo, rollField, nameField, addButton));
        rollField.textProperty().addListener((obs, oldVal, newVal) -> validateInput(classCombo, rollField, nameField, addButton));
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateInput(classCombo, rollField, nameField, addButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(classCombo.getValue(), new String[]{rollField.getText(), nameField.getText()});
            }
            return null;
        });

        Optional<Pair<String, String[]>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            String className = pair.getKey();
            String rollNo = pair.getValue()[0];
            String studentName = pair.getValue()[1];

            studentList.add(new Student(rollNo + " (" + className + ")", studentName, false));
        });
    }


    private void deleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            studentList.remove(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a student to delete!", ButtonType.OK);
            alert.showAndWait();
        }
    }


    private void validateInput(ComboBox<String> classCombo, TextField rollField, TextField nameField, Node addButton) {
        addButton.setDisable(classCombo.getValue() == null || rollField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty());
    }
}
