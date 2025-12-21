package com.example.myapp;

import java.sql.Connection;
import java.sql.Statement;

public class DBinit {

    public static void createTables() {
        // SQL string for your students table
        String studentTable = """
    CREATE TABLE IF NOT EXISTS students (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        class VARCHAR(50),
        roll_no VARCHAR(50) UNIQUE,
        name VARCHAR(100),
        email VARCHAR(100) UNIQUE,
        password VARCHAR(255)
    );
""";



        String attendanceTable = """
            CREATE TABLE IF NOT EXISTS attendance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                present INTEGER NOT NULL,
                FOREIGN KEY(student_id) REFERENCES students(id)
            );
        """;

        String userTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                role TEXT
            );
        """;

        try (Connection conn = DButil.getConnection();
             Statement stmt = conn.createStatement()) {


            stmt.execute(studentTable);
            stmt.execute(attendanceTable);
            stmt.execute(userTable);
            System.out.println("Database ready!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
