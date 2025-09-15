package com.caslanqa.assetsmanagement;

import com.caslanqa.service.AssetService;
import com.caslanqa.utils.DateUtils;
import com.caslanqa.utils.DbHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.caslanqa.utils.FormUtils.*;

public class AssetsManagementsController {

    Map<String, String> values = new HashMap<>();

    @FXML
    private TabPane analysisTabPane;
    @FXML
    private TextField ataLiraField;
    @FXML
    private TextField ataLiraFieldCurrencies;
    @FXML
    private TextField bilezikField;
    @FXML
    private TextField bilezikFieldCurrencies;
    @FXML
    private TextField ceyrekAltinField;
    @FXML
    private TextField ceyrekAltinFieldCurrencies;
    @FXML
    private Label dateLabel;
    @FXML
    private DatePicker datePickerCurrencies;
    @FXML
    private TextField dolarField;
    @FXML
    private TextField dolarFieldCurrencies;
    @FXML
    private TextField euroField;
    @FXML
    private TextField euroFieldCurrencies;
    @FXML
    private LineChart<String, Number> fiatGrowthChart;
    @FXML
    private LineChart<String, Number> goldUnitChart;
    @FXML
    private LineChart<String, Number> goldWeightChart;
    @FXML
    private TextField gramAltinField;
    @FXML
    private TextField gramAltinFieldCurrencies;
    @FXML
    private TextField tamAltinField;
    @FXML
    private TextField tamAltinFieldCurrencies;
    @FXML
    private LineChart<String, Number> totalTRYAssetsChart;
    @FXML
    private LineChart<String, Number> totalUSDAssetsChart;
    @FXML
    private Tab totalOverviewTab;
    @FXML
    private Label totalTryDisplay;
    @FXML
    private Label totalUsdDisplay;
    @FXML
    private TextField tryField;
    @FXML
    private Tab unitBasedTab;
    @FXML
    private Label dateLabelCurrencies;
    @FXML
    private TextField yarimAltinField;
    @FXML
    private TextField yarimAltinFieldCurrencies;

    private ObservableList<Map<String, String>> assetsData = FXCollections.observableArrayList();
    private ObservableList<Map<String, String>> currenciesData = FXCollections.observableArrayList();

    @FXML
    void closeBtn(MouseEvent event) {
        System.exit(0);
    }

    @FXML
    void recordAssetsBtn(MouseEvent event) throws SQLException {
        recordAssets("+");
        AssetService.insertNetAssets(AssetService.calculateLatestAssetsValues());
        clearFormFields();
    }

    @FXML
    void deleteAssetsBtn(MouseEvent event) throws SQLException {
        recordAssets("-");
        AssetService.insertNetAssets(AssetService.calculateLatestAssetsValues());
        clearFormFields();
    }

    @FXML
    void setCurrenciesBtn(MouseEvent event) throws SQLException {
        collectCurrenciesFormValues();
        clearOverviewFormFields();
        recordCurrencies();
        refreshAssetsChart();
    }

    private void clearFormFields() {
        if (euroField != null) euroField.clear();
        if (dolarField != null) dolarField.clear();
        if (bilezikField != null) bilezikField.clear();
        if (ataLiraField != null) ataLiraField.clear();
        if (tamAltinField != null) tamAltinField.clear();
        if (yarimAltinField != null) yarimAltinField.clear();
        if (ceyrekAltinField != null) ceyrekAltinField.clear();
        if (gramAltinField != null) gramAltinField.clear();
        if (tryField != null) tryField.clear();
    }

    private void clearOverviewFormFields() {
        if (datePickerCurrencies != null) datePickerCurrencies.setValue(null);
        if (euroFieldCurrencies != null) euroFieldCurrencies.clear();
        if (dolarFieldCurrencies != null) dolarFieldCurrencies.clear();
        if (bilezikFieldCurrencies != null) bilezikFieldCurrencies.clear();
        if (ataLiraFieldCurrencies != null) ataLiraFieldCurrencies.clear();
        if (tamAltinFieldCurrencies != null) tamAltinFieldCurrencies.clear();
        if (yarimAltinFieldCurrencies != null) yarimAltinFieldCurrencies.clear();
        if (ceyrekAltinFieldCurrencies != null) ceyrekAltinFieldCurrencies.clear();
        if (gramAltinFieldCurrencies != null) gramAltinFieldCurrencies.clear();
    }

    private void collectFormValues() {
        values.clear();
        values.put("euro", euroField != null && !euroField.getText().isEmpty() ? euroField.getText() : "0");
        values.put("dolar", dolarField != null && !dolarField.getText().isEmpty() ? dolarField.getText() : "0");
        values.put("bilezik", bilezikField != null && !bilezikField.getText().isEmpty() ? bilezikField.getText() : "0");
        values.put("ataLira", ataLiraField != null && !ataLiraField.getText().isEmpty() ? ataLiraField.getText() : "0");
        values.put("tamAltin", tamAltinField != null && !tamAltinField.getText().isEmpty() ? tamAltinField.getText() : "0");
        values.put("yarimAltin", yarimAltinField != null && !yarimAltinField.getText().isEmpty() ? yarimAltinField.getText() : "0");
        values.put("ceyrekAltin", ceyrekAltinField != null && !ceyrekAltinField.getText().isEmpty() ? ceyrekAltinField.getText() : "0");
        values.put("gramAltin", gramAltinField != null && !gramAltinField.getText().isEmpty() ? gramAltinField.getText() : "0");
        values.put("tl", tryField != null && !tryField.getText().isEmpty() ? tryField.getText() : "0");
    }

    private void collectCurrenciesFormValues() {
        values.clear();
        values.put("euro", euroFieldCurrencies != null && !euroFieldCurrencies.getText().isEmpty() ? euroFieldCurrencies.getText() : "0");
        values.put("dolar", dolarFieldCurrencies != null && !dolarFieldCurrencies.getText().isEmpty() ? dolarFieldCurrencies.getText() : "0");
        values.put("bilezik", bilezikFieldCurrencies != null && !bilezikFieldCurrencies.getText().isEmpty() ? bilezikFieldCurrencies.getText() : "0");
        values.put("ataLira", ataLiraFieldCurrencies != null && !ataLiraFieldCurrencies.getText().isEmpty() ? ataLiraFieldCurrencies.getText() : "0");
        values.put("tamAltin", tamAltinFieldCurrencies != null && !tamAltinFieldCurrencies.getText().isEmpty() ? tamAltinFieldCurrencies.getText() : "0");
        values.put("yarimAltin", yarimAltinFieldCurrencies != null && !yarimAltinFieldCurrencies.getText().isEmpty() ? yarimAltinFieldCurrencies.getText() : "0");
        values.put("ceyrekAltin", ceyrekAltinFieldCurrencies != null && !ceyrekAltinFieldCurrencies.getText().isEmpty() ? ceyrekAltinFieldCurrencies.getText() : "0");
        values.put("gramAltin", gramAltinFieldCurrencies != null && !gramAltinFieldCurrencies.getText().isEmpty() ? gramAltinFieldCurrencies.getText() : "0");
    }

    private void recordAssets(String recordType) {
        collectFormValues();

        values.entrySet().stream().forEach(key -> {
            try {
                DbHelper.insertAssetForCurrentUser(key.getKey(), parseDouble(key.getValue()), recordType);
            } catch (SQLException e) {
                showError("Database Error", "Failed to record assets: " + e.getMessage());
            }
        });
    }

    private void recordCurrencies() {
        values.entrySet().stream().forEach(key -> {
            try {
                DbHelper.insertCurrensies(key.getKey(), parseDouble(key.getValue()));
            } catch (SQLException e) {
                showError("Database Error", "Failed to record assets: " + e.getMessage());
            }
        });
    }

    @FXML
    void initialize() throws SQLException {
        dateLabel.setText(DateUtils.getDateAsString(null));
        dateLabelCurrencies.setText(DateUtils.getDateAsString(null));
        loadChartData();
        populateTotalOverview();

        analysisTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> {
                    if (newTab == unitBasedTab) {
                        try {
                            refreshAssetsChart();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (newTab == totalOverviewTab) {
                        try {
                            populateTotalOverview();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    @FXML
    private void refreshAssetsChart() throws SQLException {
        loadChartData();
        populateTotalOverview();
    }

    private void loadChartData() {
        try {
            // Get raw asset data from the database.
            List<Map<String, Object>> rawData = DbHelper.getNetAssetsData(DbHelper.getCurrentUserId());

            // Convert the raw data to a list of maps with String values.
            List<Map<String, String>> convertedData = rawData.stream()
                    .map(this::convertRow) // Use a helper method for clarity and reusability
                    .collect(Collectors.toList());

            // Set the converted data to the assetsData list.
            assetsData.setAll(convertedData);

            populateAssetsChart();
        } catch (Exception e) {
            showError("Hata", "Veri yüklenirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateTotalOverview() throws SQLException {
        // 1. Get net asset values from the database
        Map<String, Double> netAssetValues = AssetService.getLastNetAssets().entrySet().stream()
                .filter(entry -> entry.getKey() != null && !"created_at".equals(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> ((Number) entry.getValue()).doubleValue()
                ));

        if (netAssetValues.isEmpty()) {
            totalTRYAssetsChart.getData().clear();
            totalUSDAssetsChart.getData().clear();
            totalTryDisplay.setText("0.00 ₺");
            totalUsdDisplay.setText("0.00 $");
            return;
        }

        // 2. Get the latest currency rates from the database
        Map<String, Double> rates = DbHelper.getLatestCurrencies();

        if (rates.isEmpty()) {
            return;
        }

        // 3. Calculate the total asset value in TRY and USD
        double totalTry = 0.0;

        totalTry += netAssetValues.getOrDefault("euro", 0.0) * rates.getOrDefault("euro", 0.0);
        totalTry += netAssetValues.getOrDefault("dolar", 0.0) * rates.getOrDefault("dolar", 0.0);
        totalTry += netAssetValues.getOrDefault("bilezik", 0.0) * rates.getOrDefault("bilezik", 0.0);
        totalTry += netAssetValues.getOrDefault("ataLira", 0.0) * rates.getOrDefault("ataLira", 0.0);
        totalTry += netAssetValues.getOrDefault("tamAltin", 0.0) * rates.getOrDefault("tamAltin", 0.0);
        totalTry += netAssetValues.getOrDefault("yarimAltin", 0.0) * rates.getOrDefault("yarimAltin", 0.0);
        totalTry += netAssetValues.getOrDefault("ceyrekAltin", 0.0) * rates.getOrDefault("ceyrekAltin", 0.0);
        totalTry += netAssetValues.getOrDefault("gramAltin", 0.0) * rates.getOrDefault("gramAltin", 0.0);
        totalTry += netAssetValues.getOrDefault("tl", 0.0);

        double totalUsd = rates.getOrDefault("dolar", 0.0) > 0 ? totalTry / rates.getOrDefault("dolar", 0.0) : 0.0;

        // 4. Update the chart and display labels with the single, calculated total
        totalTRYAssetsChart.getData().clear();
        totalUSDAssetsChart.getData().clear();

        XYChart.Series<String, Number> trySeries = new XYChart.Series<>();
        trySeries.setName("Toplam ₺");
        XYChart.Series<String, Number> usdSeries = new XYChart.Series<>();
        usdSeries.setName("Toplam $");

        // We no longer have data by date, so we will create a single point for "Today" or a similar label
        String dateLabel = "Bugün";
        trySeries.getData().add(new XYChart.Data<>(dateLabel, totalTry));
        usdSeries.getData().add(new XYChart.Data<>(dateLabel, totalUsd));

        totalTRYAssetsChart.getData().add(trySeries);
        totalUSDAssetsChart.getData().add(usdSeries);

        totalTryDisplay.setText(String.format("%,.2f ₺", totalTry));
        totalUsdDisplay.setText(String.format("%,.2f $", totalUsd));

        // Optional: Add tooltips for the single data point
        double finalTotalTry = totalTry;
        trySeries.getData().get(0).nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip tooltip = new Tooltip(dateLabel + "\nToplam ₺: " + String.format("%,.2f", finalTotalTry));
                Tooltip.install(newNode, tooltip);
            }
        });
        usdSeries.getData().get(0).nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip tooltip = new Tooltip(dateLabel + "\nToplam $: " + String.format("%,.2f", totalUsd));
                Tooltip.install(newNode, tooltip);
            }
        });
    }

    private void populateAssetsChart() {
        if (assetsData.isEmpty()) {
            goldWeightChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            fiatGrowthChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            goldUnitChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            return;
        }

        goldWeightChart.getData().clear();
        fiatGrowthChart.getData().clear();
        goldUnitChart.getData().clear();

        // Veritabanı şemasına göre sabit varlık listeleri
        List<String> fiatAssets = Arrays.asList("euro", "dolar", "tl");
        List<String> goldWeightAssets = Arrays.asList("bilezik");
        List<String> goldUnitAssets = Arrays.asList("gramAltin", "ataLira", "tamAltin", "yarimAltin", "ceyrekAltin");

        // Tarih alanına göre verileri sırala
        List<Map<String, String>> sortedData = assetsData.stream()
                .sorted(Comparator.comparingLong(m -> {
                    String dateString = m.get("created_at");
                    if (dateString == null || dateString.isEmpty()) {
                        return 0L;
                    }
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        return sdf.parse(dateString).getTime();
                    } catch (ParseException e) {
                        System.err.println("Tarih formatlama hatası: " + dateString);
                        e.printStackTrace();
                        return 0L;
                    }
                }))
                .collect(Collectors.toList());

        // Her bir grafik için ilgili varlık serilerini ekle
        addSeriesToChart(fiatGrowthChart, sortedData, fiatAssets);
        addSeriesToChart(goldWeightChart, sortedData, goldWeightAssets);
        addSeriesToChart(goldUnitChart, sortedData, goldUnitAssets);
    }

    private void addSeriesToChart(LineChart<String, Number> chart,
                                  List<Map<String, String>> sortedData,
                                  List<String> assetTypes) {
        for (String assetType : assetTypes) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(getChartDisplayName(assetType));

            for (Map<String, String> row : sortedData) {
                String date = formatDate(row.get("created_at"));
                String valueStr = row.get(assetType);
                if (valueStr != null && !valueStr.trim().isEmpty()) {
                    try {
                        double value = Double.parseDouble(valueStr);
                        XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, value);
                        series.getData().add(dataPoint);

                        // Tooltip ekleme
                        dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                            if (newNode != null) {
                                Tooltip tooltip = new Tooltip(date + "\n" + getChartDisplayName(assetType) + ": " + value);
                                tooltip.setShowDelay(Duration.millis(100));
                                Tooltip.install(newNode, tooltip);
                            }
                        });

                    } catch (NumberFormatException ignored) {
                        // Sayıya dönüştürülemeyen değerleri atla
                    }
                }
            }

            if (!series.getData().isEmpty()) {
                chart.getData().add(series);
            }
        }
    }

    private Map<String, String> convertRow(Map<String, Object> row) {
        return row.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Object value = entry.getValue();
                            if (value == null) {
                                return ""; // Return empty string for null values
                            }
                            if (value instanceof Timestamp) {
                                Timestamp timestamp = (Timestamp) value;
                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                return sdf.format(timestamp);
                            } else {
                                return value.toString(); // Convert other types to String
                            }
                        }
                ));
    }
}