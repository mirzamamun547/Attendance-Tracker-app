package com.example.myapp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBinit {

    public static void createTables() {


        String userTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                role TEXT
            );
        """;


        String classTable = """
            CREATE TABLE IF NOT EXISTS classes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                teacher_id INTEGER NOT NULL,
                class_name TEXT NOT NULL,
                FOREIGN KEY (teacher_id) REFERENCES users(id)
            );
        """;


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


        String studentClassesTable = """
            CREATE TABLE IF NOT EXISTS student_classes (
                student_id INTEGER NOT NULL,
                class_id INTEGER NOT NULL,
                PRIMARY KEY (student_id, class_id),
                FOREIGN KEY (student_id) REFERENCES students(id),
                FOREIGN KEY (class_id) REFERENCES classes(id)
            );
        """;


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

        String leaveRequestsTable = """
            CREATE TABLE IF NOT EXISTS leave_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                class_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                reason TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                FOREIGN KEY (student_id) REFERENCES students(id),
                FOREIGN KEY (class_id) REFERENCES classes(id)
            );
        """;

        try (Connection conn = DButil.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(userTable);
            stmt.execute(classTable);
            stmt.execute(studentTable);
            stmt.execute(studentClassesTable);
            stmt.execute(attendanceTable);
            stmt.execute(leaveRequestsTable);

            System.out.println("✅ Database initialized successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
