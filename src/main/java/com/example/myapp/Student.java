package com.example.myapp;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Student {

    private final StringProperty rollNo = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty present = new SimpleBooleanProperty();


    public Student(String rollNo, String name, boolean present) {
        this.rollNo.set(rollNo);
        this.name.set(name);
        this.present.set(present);
    }


    public String getRollNo() {
        return rollNo.get();
    }
    public void setRollNo(String rollNo) {
        this.rollNo.set(rollNo);
    }
    public StringProperty rollNoProperty() {
        return rollNo;
    }


    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public StringProperty nameProperty() {
        return name;
    }


    public boolean isPresent() {
        return present.get();
    }
    public void setPresent(boolean present) {
        this.present.set(present);
    }
    public BooleanProperty presentProperty() {
        return present;
    }
}
