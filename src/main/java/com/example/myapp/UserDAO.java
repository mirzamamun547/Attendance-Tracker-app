package com.example.myapp;

import java.sql.*;

public class UserDAO {


    public static boolean register(String name, String email, String password, String role) {
        String sql = "INSERT INTO users(name,email,password,role) VALUES(?,?,?,?)";

        try (Connection conn = DButil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }


    public static boolean login(String email, String password, String role) {
        String sql;

        if ("Student".equalsIgnoreCase(role)) {

            sql = "SELECT * FROM students WHERE email=? AND password=?";
        } else {

            sql = "SELECT * FROM users WHERE email=? AND password=? AND role=?";
        }

        try (Connection conn = DButil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            if (!"Student".equalsIgnoreCase(role)) {
                ps.setString(3, role);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
