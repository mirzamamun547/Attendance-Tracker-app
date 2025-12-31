package com.example.myapp;
import javafx.beans.property.*;

public class Student {

    private final IntegerProperty id;
    private final StringProperty rollNo;
    private final StringProperty name;
    private final StringProperty className;  // ✅ new
    private final BooleanProperty present;

    public Student(int id, String rollNo, String name, boolean present, String className) {
        this.id = new SimpleIntegerProperty(id);
        this.rollNo = new SimpleStringProperty(rollNo);
        this.name = new SimpleStringProperty(name);
        this.present = new SimpleBooleanProperty(present);
        this.className = new SimpleStringProperty(className);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getRollNo() { return rollNo.get(); }
    public StringProperty rollNoProperty() { return rollNo; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public boolean isPresent() { return present.get(); }
    public BooleanProperty presentProperty() { return present; }

    public String getClassName() { return className.get(); }
    public StringProperty classNameProperty() { return className; }
}
