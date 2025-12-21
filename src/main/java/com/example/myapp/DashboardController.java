package com.example.myapp;

import javafx.beans.value.ChangeListener;
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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;

public class DashboardController {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> rollCol;
    @FXML private TableColumn<Student, String> nameCol;
    @FXML private TableColumn<Student, Boolean> presentCol;
    @FXML private Button addStudentBtn;
    @FXML private Button deleteStudentBtn;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final ObservableList<String> classes = FXCollections.observableArrayList("Class 1","Class 2","Class 3");

    @FXML
    public void initialize() {
        rollCol.setCellValueFactory(c -> c.getValue().rollNoProperty());
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        presentCol.setCellValueFactory(c -> c.getValue().presentProperty());
        presentCol.setCellFactory(CheckBoxTableCell.forTableColumn(presentCol));
        studentTable.setEditable(true);
        studentTable.setItems(studentList);
        loadStudents();
        addStudentBtn.setOnAction(e -> addStudent());
        deleteStudentBtn.setOnAction(e -> deleteSelectedStudent());
    }
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

    private void loadStudents() {
        studentList.clear();
        String sql = "SELECT id, roll_no, name FROM students";
        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                studentList.add(new Student(rs.getInt("id"),
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String generatePassword() {
        // For demo: 8‑char alphanumeric
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }
    private void addStudent() {
        Dialog<StudentData> dialog = new Dialog<>();
        dialog.setTitle("Add Student");

        // add standard OK and Cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> classBox = new ComboBox<>(classes);
        TextField rollField = new TextField();
        TextField nameField = new TextField();
        classBox.setPromptText("Class");
        rollField.setPromptText("Roll No");
        nameField.setPromptText("Name");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Class:"), classBox);
        grid.addRow(1, new Label("Roll:"), rollField);
        grid.addRow(2, new Label("Name:"), nameField);
        dialog.getDialogPane().setContent(grid);

        // get the OK button and disable it initially
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // validation: disable until all fields are filled
        ChangeListener<String> validator = (obs, oldVal, newVal) ->
                okButton.setDisable(classBox.getValue() == null ||
                        rollField.getText().isBlank() ||
                        nameField.getText().isBlank());
        classBox.valueProperty().addListener((obs, o, n) -> validator.changed(null, null, null));
        rollField.textProperty().addListener(validator);
        nameField.textProperty().addListener(validator);

        // convert result into a StudentData record
        dialog.setResultConverter(btn ->
                btn.getButtonData() == ButtonBar.ButtonData.OK_DONE
                        ? new StudentData(classBox.getValue(), rollField.getText(), nameField.getText())
                        : null);

        dialog.showAndWait().ifPresent(data -> {
            String password = generatePassword();
            String email = data.roll(); // or data.roll() + "@student.local"
            insertStudent(data.clazz(), data.roll(), data.name(), email, password);

            new Alert(Alert.AlertType.INFORMATION,
                    "Email: " + email + "\nPassword: " + password)
                    .showAndWait();
        });
    }


    private record StudentData(String clazz, String roll, String name) {}




    private void insertStudent(String clazz, String roll, String name, String email, String password) {
        System.out.println("insertStudent called with: " + roll + ", " + name);

        String sql = "INSERT INTO students (class, roll_no, name, email, password) VALUES (?,?,?,?,?)";
        try (Connection conn = DButil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clazz);
            ps.setString(2, roll);
            ps.setString(3, name);
            ps.setString(4, email);
            ps.setString(5, password);
            int rows = ps.executeUpdate();
            System.out.println("Rows inserted: " + rows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void deleteSelectedStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if(selected==null){ new Alert(Alert.AlertType.WARNING,"Select a student").show(); return;}
        String sql="DELETE FROM students WHERE id=?";
        try(Connection con=DButil.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,selected.getId());
            ps.executeUpdate();
            studentList.remove(selected);
        } catch(Exception e){ e.printStackTrace();}
    }

    private void validate(ComboBox<String> c, TextField r, TextField n, Node b){
        b.setDisable(c.getValue()==null || r.getText().isBlank() || n.getText().isBlank());
    }
}
