package com.example.myapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static Integer login(String email, String password, String role) {
        String sql;
        if ("Student".equalsIgnoreCase(role)) {
            sql = "SELECT id FROM students WHERE email=? AND password=?";
        } else if ("Teacher".equalsIgnoreCase(role)) {
            sql = "SELECT id FROM users WHERE email=? AND password=? AND role=?";
        } else {
            return null;
        }


        try (Connection con = DButil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            if ("Teacher".equalsIgnoreCase(role)) {
                ps.setString(3, role); // only for teacher
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // return user ID
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // login failed
    }
}
