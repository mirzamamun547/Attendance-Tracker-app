package com.example.myapp;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttendanceRecord {
    private final StringProperty date;
    private final StringProperty status;
    private final StringProperty remarks;

    public AttendanceRecord(String date, String status, String remarks) {
        this.date = new SimpleStringProperty(date);
        this.status = new SimpleStringProperty(status);
        this.remarks = new SimpleStringProperty(remarks);
    }

    public StringProperty dateProperty() { return date; }
    public StringProperty statusProperty() { return status; }
    public StringProperty remarksProperty() { return remarks; }

    public String getDate() { return date.get(); }
    public String getStatus() { return status.get(); }
    public String getRemarks() { return remarks.get(); }
}
