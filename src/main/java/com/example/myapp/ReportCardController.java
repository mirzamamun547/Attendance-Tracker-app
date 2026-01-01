package com.example.myapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReportCardController {
    @FXML private Label rollLabel;
    @FXML private Label nameLabel;
    @FXML private Label statusLabel;
    @FXML private Label remarksLabel;

    public void setData(String roll, String name, String status, String remarks) {
        rollLabel.setText("Roll: " + roll);
        nameLabel.setText("Name: " + name);
        statusLabel.setText("Status: " + status);
        remarksLabel.setText("Reason: " + (remarks == null || remarks.isBlank() ? "—" : remarks));
    }
}

