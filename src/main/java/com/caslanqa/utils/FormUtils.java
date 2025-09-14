package com.caslanqa.utils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FormUtils {

    public static String formatDate(String rawDate) {
        try {
            if (rawDate.contains("-")) {
                LocalDate date = LocalDate.parse(rawDate);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
                return date.format(formatter);
            }
            return rawDate;
        } catch (Exception e) {
            return rawDate;
        }
    }

    public static double parseDouble(String value) {
        try {
            return (value != null && !value.isEmpty()) ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showSystemError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean checkUsernamePassword(String user, String pass) {
        if (user.isEmpty()) {
            showError("Validation Error", "Username cannot be empty.");
            return false;
        }

        if (pass.isEmpty()) {
            showError("Validation Error", "Password cannot be empty.");
            return false;
        }

        if (pass.length()<6) {
            showError("Validation Error", "Password must be at least 6 characters long.");
            return false;
        }

        return true;
    }

    public static String getChartDisplayName(String fieldName) {
        Map<String, String> displayNames = Map.of(
                "euro", "Euro (€)",
                "dolar", "Dolar ($)",
                "bilezik", "Bilezik",
                "ataLira", "Ata Lira",
                "tamAltin", "Tam Altın",
                "yarimAltin", "Yarım Altın",
                "ceyrekAltin", "Çeyrek Altın",
                "gramAltin", "Gram Altın",
                "tl", "TL (₺)"
        );
        return displayNames.getOrDefault(fieldName, fieldName);
    }
}
