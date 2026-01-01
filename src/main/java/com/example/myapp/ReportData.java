package com.example.myapp;

public class ReportData {
    private final String rollNo;
    private final String name;
    private final String status;

    public ReportData(String rollNo, String name, String status) {
        this.rollNo = rollNo;
        this.name = name;
        this.status = status;
    }

    public String getRollNo() { return rollNo; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}
