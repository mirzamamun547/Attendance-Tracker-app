package com.example.myapp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBinit {

    public static void createTables() {

        // USERS (teachers + students)
        String userTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                role TEXT
            );
        """;

        // CLASSES (created by teachers)
        String classTable = """
            CREATE TABLE IF NOT EXISTS classes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                teacher_id INTEGER NOT NULL,
                class_name TEXT NOT NULL,
                FOREIGN KEY (teacher_id) REFERENCES users(id)
            );
        """;

        // STUDENTS (basic info, no longer tied to one class only)
        String studentTable = """
            CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                teacher_id INTEGER NOT NULL,
                roll_no TEXT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                FOREIGN KEY (teacher_id) REFERENCES users(id)
            );
        """;

        // STUDENT_CLASSES (join table for many-to-many relationship)
        String studentClassesTable = """
            CREATE TABLE IF NOT EXISTS student_classes (
                student_id INTEGER NOT NULL,
                class_id INTEGER NOT NULL,
                PRIMARY KEY (student_id, class_id),
                FOREIGN KEY (student_id) REFERENCES students(id),
                FOREIGN KEY (class_id) REFERENCES classes(id)
            );
        """;

        // ATTENDANCE (linked to student + class, with remarks)
        String attendanceTable = """
            CREATE TABLE IF NOT EXISTS attendance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                class_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                present INTEGER NOT NULL,
                remarks TEXT,
                FOREIGN KEY (student_id) REFERENCES students(id),
                FOREIGN KEY (class_id) REFERENCES classes(id),
                UNIQUE(student_id, class_id, date)
            );
        """;

        try (Connection conn = DButil.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(userTable);
            stmt.execute(classTable);
            stmt.execute(studentTable);
            stmt.execute(studentClassesTable);   // ✅ now included
            stmt.execute(attendanceTable);

            System.out.println("✅ Database initialized successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
