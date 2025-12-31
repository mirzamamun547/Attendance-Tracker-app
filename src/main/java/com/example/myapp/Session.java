package com.example.myapp;

public class Session {

    private static int userId;
    private static String role;

    public static void set(int id, String r) {
        userId = id;
        role = r;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getRole() {
        return role;
    }
}
