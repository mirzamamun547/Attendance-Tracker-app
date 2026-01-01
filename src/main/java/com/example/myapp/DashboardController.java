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

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DashboardController {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> rollCol;
    @FXML private TableColumn<Student, String> nameCol;
    @FXML private TableColumn<Student, Boolean> presentCol;
    @FXML private Button addStudentBtn;
    @FXML private Button deleteStudentBtn;
    @FXML private Button addClassBtn; // Create class
    @FXML private DatePicker datePicker;
@FXML
    private int currentTeacherId;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final ObservableList<String> classes = FXCollections.observableArrayList();
    private final Map<String, Integer> classMap = new HashMap<>();

    private void loadStudents() {
        studentList.clear();
        String sql = """
        SELECT s.id, s.roll_no, s.name, c.class_name
        FROM students s
        JOIN student_classes sc ON s.id = sc.student_id
        JOIN classes c ON sc.class_id = c.id
        WHERE c.teacher_id = ?
    """;

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentTeacherId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                studentList.add(new Student(
                        rs.getInt("id"),
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        false,
                        rs.getString("class_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Fixed CheckBox column
    @FXML
    public void initialize() {
        rollCol.setCellValueFactory(c -> c.getValue().rollNoProperty());
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        presentCol.setCellValueFactory(c -> c.getValue().presentProperty());
        presentCol.setCellFactory(tc -> new CheckBoxTableCell<>());

        studentTable.setItems(studentList);

        addStudentBtn.setOnAction(e -> addStudent());
        deleteStudentBtn.setOnAction(e -> deleteSelectedStudent());
        addClassBtn.setOnAction(e -> createClass());
    }


    public void setCurrentTeacherId(int teacherId) {
        this.currentTeacherId = teacherId;
        loadClasses();
        loadStudents();
    }

    private void loadClasses() {
        classes.clear();
        classMap.clear();
        String sql = "SELECT id, class_name FROM classes WHERE teacher_id=?";
        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentTeacherId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
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
    private void createClass() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Class");
        dialog.setHeaderText("Enter class name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String sql = "INSERT INTO classes (teacher_id, class_name) VALUES (?,?)";
            try (Connection con = DButil.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, currentTeacherId);
                ps.setString(2, name);
                ps.executeUpdate();
                loadClasses();
                new Alert(Alert.AlertType.INFORMATION, "Class created successfully!").show();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error creating class!").show();
            }
        });
    }

    private void loadStudentsForClass(String className) {
        studentList.clear();
        Integer classId = classMap.get(className);
        if (classId == null) return;

        String sql = "SELECT id, roll_no, name FROM students WHERE teacher_id=? AND class_id=?";
        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentTeacherId);
            ps.setInt(2, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                studentList.add(new Student(
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

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }

    private void addStudent() {
        Dialog<StudentData> dialog = new Dialog<>();
        dialog.setTitle("Add Student");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> classBox = new ComboBox<>(classes);
        TextField rollField = new TextField();
        TextField nameField = new TextField();
        classBox.setPromptText("Class");
        rollField.setPromptText("Roll No");
        nameField.setPromptText("Name");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Class:"), classBox);
        grid.addRow(1, new Label("Roll:"), rollField);
        grid.addRow(2, new Label("Name:"), nameField);
        dialog.getDialogPane().setContent(grid);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        ChangeListener<String> validator = (obs, oldVal, newVal) ->
                okButton.setDisable(classBox.getValue()==null || rollField.getText().isBlank() || nameField.getText().isBlank());
        classBox.valueProperty().addListener((obs,o,n) -> validator.changed(null,null,null));
        rollField.textProperty().addListener(validator);
        nameField.textProperty().addListener(validator);

        dialog.setResultConverter(btn ->
                btn.getButtonData()==ButtonBar.ButtonData.OK_DONE ?
                        new StudentData(classBox.getValue(), rollField.getText(), nameField.getText()) : null);

        dialog.showAndWait().ifPresent(data -> {
            String password = generatePassword();
            String email = data.roll() + "@school.local";
            int classId = classMap.get(data.clazz());
            insertStudent(currentTeacherId, classId, data.roll(), data.name(), email, password);
            new Alert(Alert.AlertType.INFORMATION, "Email: " + email + "\nPassword: " + password).show();
        });
    }
    private void insertStudent(int teacherId, int classId, String roll, String name, String email, String password) {
        String checkSql = "SELECT id FROM students WHERE roll_no=? AND name=?";
        try (Connection conn = DButil.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setString(1, roll);
            checkPs.setString(2, name);
            ResultSet rs = checkPs.executeQuery();

            int studentId;
            if (rs.next()) {
                // Student already exists → reuse their ID
                studentId = rs.getInt("id");
                System.out.println("Existing student found: " + name + " (" + roll + ")");
            } else {
                // New student → insert with password
                String insertSql = "INSERT INTO students (teacher_id, roll_no, name, email, password) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, teacherId);
                    ps.setString(2, roll);
                    ps.setString(3, name);
                    ps.setString(4, email);
                    ps.setString(5, password);
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    keys.next();
                    studentId = keys.getInt(1);
                    System.out.println("New student inserted: " + name + " (" + roll + ")");
                }
            }

            // ✅ Ensure class exists
            String classCheckSql = "SELECT id FROM classes WHERE id=?";
            try (PreparedStatement classCheckPs = conn.prepareStatement(classCheckSql)) {
                classCheckPs.setInt(1, classId);
                ResultSet classRs = classCheckPs.executeQuery();
                if (!classRs.next()) {
                    String insertClassSql = "INSERT INTO classes (id, teacher_id, class_name) VALUES (?, ?, ?)";
                    try (PreparedStatement insertClassPs = conn.prepareStatement(insertClassSql)) {
                        insertClassPs.setInt(1, classId);
                        insertClassPs.setInt(2, teacherId);
                        insertClassPs.setString(3, "Class " + classId);
                        insertClassPs.executeUpdate();
                        System.out.println("Class added: " + classId);
                    }
                }
            }

            // ✅ Link student to class (no overwrite, no password)
            String linkSql = "INSERT OR IGNORE INTO student_classes (student_id, class_id) VALUES (?, ?)";
            try (PreparedStatement linkPs = conn.prepareStatement(linkSql)) {
                linkPs.setInt(1, studentId);
                linkPs.setInt(2, classId);
                linkPs.executeUpdate();
                System.out.println("Student " + studentId + " linked to class " + classId);
            }

            loadStudents();

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
    private void openAttendance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/myapp/attendance.fxml"));

            Parent root = loader.load();
            attendanceController controller = loader.getController();
            controller.setCurrentTeacherId(currentTeacherId);
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
    private void openReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/myapp/reports.fxml"));
            Parent root = loader.load();

            reportsController controller = loader.getController();
            controller.setCurrentTeacherId(currentTeacherId); // ✅ IMPORTANT

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Reports");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadAttendanceByDate() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a date").show();
            return;
        }

        studentList.clear();

        String sql = """
        SELECT s.id, s.roll_no, s.name, c.class_name,
               COALESCE(a.present, 0) AS present
        FROM students s
        JOIN classes c ON s.class_id = c.id
        LEFT JOIN attendance a
        ON s.id = a.student_id AND a.date = ?
        WHERE s.teacher_id = ?
        """;

        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ps.setInt(2, currentTeacherId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                studentList.add(new Student(
                        rs.getInt("id"),
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getInt("present") == 1,
                        rs.getString("class_name")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading attendance").show();
        }
    }

    private record StudentData(String clazz, String roll, String name) {}
}
