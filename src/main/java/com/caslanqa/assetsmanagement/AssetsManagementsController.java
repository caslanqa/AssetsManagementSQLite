package com.caslanqa.assetsmanagement;

import com.caslanqa.service.AssetService;
import com.caslanqa.service.CurrencyService;
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
    private TextField bilezikField;

    @FXML
    private TextField ceyrekAltinField;

    @FXML
    private TextField dolarField;

    @FXML
    private TextField euroField;

    @FXML
    private LineChart<String, Number> euroGrowthChart;

    @FXML
    private TabPane functionTabs;

    @FXML
    private LineChart<String, Number> goldUnitChart;

    @FXML
    private LineChart<String, Number> goldWeightChart;

    @FXML
    private TextField gramAltinField;

    @FXML
    private TextField tamAltinField;

    @FXML
    private Tab totalOverviewTab;

    @FXML
    private LineChart<String, Number> totalTRYAssetsChart;

    @FXML
    private Label totalTryDisplay;

    @FXML
    private LineChart<String, Number> totalUSDAssetsChart;

    @FXML
    private Label totalUsdDisplay;

    @FXML
    private TextField tryField;

    @FXML
    private LineChart<String, Number> tryGrowthChart;

    @FXML
    private Tab unitBasedTab;

    @FXML
    private LineChart<String, Number> usdGrowthChart;

    @FXML
    private TextField yarimAltinField;

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
        populateTotalOverview();
        refreshAssetsChart();
    }

    @FXML
    void deleteAssetsBtn(MouseEvent event) throws SQLException {
        recordAssets("-");
        AssetService.insertNetAssets(AssetService.calculateLatestAssetsValues());
        clearFormFields();
        populateTotalOverview();
        refreshAssetsChart();
    }

    @FXML
    void setCurrenciesBtn(MouseEvent event) throws Exception {
        recordCurrencies();
        refreshAssetsChart();
        populateTotalOverview();
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

    private Map<String, String> collectCurrencies() throws Exception {
        Map<String, String> currencies = new HashMap<>();
        currencies.putAll(CurrencyService.fetchUsdEurTryRates(DbHelper.API_KEY));
        currencies.putAll(CurrencyService.fetchGoldPrices(DbHelper.API_KEY));
        return currencies;
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

    private void recordCurrencies() throws Exception {

        collectCurrencies().entrySet().stream().forEach(key -> {
            try {
                DbHelper.insertCurrencies(key.getKey(), parseDouble(key.getValue()));
            } catch (SQLException e) {
                showError("Database Error", "Failed to record assets: " + e.getMessage());
            }
        });
    }

    @FXML
    void initialize() throws SQLException {
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

        // Show symbols so overlapping series are still visible as points
        if (tryGrowthChart != null) {
            tryGrowthChart.setCreateSymbols(true);
        }

        if (usdGrowthChart != null) {
            usdGrowthChart.setCreateSymbols(true);
        }
        
        if (euroGrowthChart != null) {
            euroGrowthChart.setCreateSymbols(true);
        }
    }

    @FXML
    private void refreshAssetsChart() throws SQLException {
        loadChartData();
        populateTotalOverview();
    }

    private void loadChartData() {
        try {
            // Get raw asset data from the database.
            List<Map<String, String>> rawData = AssetService.getNetAssetsData();

            // Set the converted data to the assetsData list.
            assetsData.setAll(rawData);

            populateAssetsChart();
        } catch (Exception e) {
            showError("Hata", "Veri yüklenirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateTotalOverview() throws SQLException {
        // 1) Tüm net varlık verilerini tarihe göre al
        List<Map<String, String>> history = AssetService.getNetAssetsData();

        totalTRYAssetsChart.getData().clear();
        totalUSDAssetsChart.getData().clear();

        if (history == null || history.isEmpty()) {
            totalTryDisplay.setText("0.00 ₺");
            totalUsdDisplay.setText("0.00 $");
            return;
        }

        // 2) En güncel kurları al (tarihsel kur yerine basit yaklaşım)
        Map<String, Double> rates = DbHelper.getLatestCurrencies();
        if (rates.isEmpty()) {
            totalTryDisplay.setText("0.00 ₺");
            totalUsdDisplay.setText("0.00 $");
            return;
        }

        XYChart.Series<String, Number> trySeries = new XYChart.Series<>();
        trySeries.setName("Toplam ₺");
        XYChart.Series<String, Number> usdSeries = new XYChart.Series<>();
        usdSeries.setName("Toplam $");

        double lastTotalTry = 0.0;
        double lastTotalUsd = 0.0;

        for (Map<String, String> row : history) {
            String date = row.getOrDefault("created_at", "");

            double totalTry = 0.0;
            totalTry += parseDouble(row.get("euro")) * rates.getOrDefault("euro", 0.0);
            totalTry += parseDouble(row.get("dolar")) * rates.getOrDefault("dolar", 0.0);
            totalTry += parseDouble(row.get("bilezik")) * rates.getOrDefault("bilezik", 0.0);
            totalTry += parseDouble(row.get("ataLira")) * rates.getOrDefault("ataLira", 0.0);
            totalTry += parseDouble(row.get("tamAltin")) * rates.getOrDefault("tamAltin", 0.0);
            totalTry += parseDouble(row.get("yarimAltin")) * rates.getOrDefault("yarimAltin", 0.0);
            totalTry += parseDouble(row.get("ceyrekAltin")) * rates.getOrDefault("ceyrekAltin", 0.0);
            totalTry += parseDouble(row.get("gramAltin")) * rates.getOrDefault("gramAltin", 0.0);
            totalTry += parseDouble(row.get("tl"));

            double totalUsd = rates.getOrDefault("dolar", 0.0) > 0 ? totalTry / rates.getOrDefault("dolar", 0.0) : 0.0;

            trySeries.getData().add(new XYChart.Data<>(date, totalTry));
            usdSeries.getData().add(new XYChart.Data<>(date, totalUsd));

            lastTotalTry = totalTry;
            lastTotalUsd = totalUsd;
        }

        totalTRYAssetsChart.getData().add(trySeries);
        totalUSDAssetsChart.getData().add(usdSeries);

        totalTryDisplay.setText(String.format("%,.2f ₺", lastTotalTry));
        totalUsdDisplay.setText(String.format("%,.2f $", lastTotalUsd));
    }

    private void populateAssetsChart() {
        if (assetsData.isEmpty()) {
            goldWeightChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            tryGrowthChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            usdGrowthChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            euroGrowthChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            goldUnitChart.setTitle("Veri Bulunamadı - Lütfen önce varlık ekleyin");
            return;
        }

        goldWeightChart.getData().clear();
        tryGrowthChart.getData().clear();
        euroGrowthChart.getData().clear();
        usdGrowthChart.getData().clear();
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
        addSeriesToChart(usdGrowthChart, sortedData, Arrays.asList("dolar"));
        addSeriesToChart(tryGrowthChart, sortedData, Arrays.asList("tl"));
        addSeriesToChart(euroGrowthChart, sortedData, Arrays.asList("euro"));
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
                String date = row.get("created_at");
                String valueStr = row.get(assetType);
                if (valueStr != null && !valueStr.trim().isEmpty()) {
                    try {
                        double value = Double.parseDouble(valueStr);
                        // Apply tiny per-series jitter to separate overlapping series visually
                        double epsilon = 0.0001;
                        int idx = assetTypes.indexOf(assetType);
                        double jittered = value + (idx * epsilon);
                        XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, jittered);
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
}