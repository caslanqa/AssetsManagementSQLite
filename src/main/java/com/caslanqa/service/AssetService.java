package com.caslanqa.service;

import com.caslanqa.utils.DbHelper;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssetService {

    /**
     * Tüm asset kayıtlarını çekip, asset_type ve record_type (+/-) göre toplar/çıkarır.
     * @return Map<String, Double> -> asset_type -> toplam miktar
     */
    public static Map<String, Double> calculateLatestAssetsValues() throws SQLException {
        List<Map<String, Object>> assets = DbHelper.getAssetsForCurrentUser();

        Map<String, Double> result = new HashMap<>();

        for (Map<String, Object> row : assets) {
            String assetType = (String) row.get("asset_type");
            String recordType = (String) row.get("record_type");
            double quantity = ((Number) row.get("quantity")).doubleValue();

            // Mevcut değeri al, yoksa 0'dan başla
            double current = result.getOrDefault(assetType, 0.0);

            if ("+".equals(recordType)) {
                current += quantity;
            } else if ("-".equals(recordType)) {
                current -= quantity;
            }

            result.put(assetType, current);
        }

        return result;
    }

    public static void insertNetAssets(Map<String, Double> netAssets) throws SQLException {
        Map<String, Object> dataToInsert = netAssets.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        DbHelper.insert("net_assets", dataToInsert, true);
    }

    public static Map<String, String> getLastNetAssets() throws SQLException {
        List<Map<String, String>> list = getNetAssetsData();
        if (list.isEmpty()) {
            return new HashMap<>();
        }
        return list.get(list.size() - 1);
    }

    public static List<Map<String, String>> getNetAssetsData() throws SQLException {
        String sql = "SELECT euro, ataLira, tamAltin, gramAltin, ceyrekAltin, yarimAltin, bilezik, dolar, tl, created_at " +
                "FROM net_assets WHERE user_id = ? ORDER BY created_at";

        List<Map<String, Object>> rows = DbHelper.getSqlQuery(sql);

        // Convert the raw data to a list of maps with String values.
        List<Map<String, String>> convertedData = rows.stream()
                .map(row -> convertRow(row))
                .collect(Collectors.toList());

        return convertedData;
    }

    private static Map<String, String> convertRow(Map<String, Object> row) {
        return row.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Object value = entry.getValue();
                            if (value == null) {
                                return "";
                            }
                            String key = entry.getKey();
                            if ("created_at".equals(key)) {
                                // Normalize to dd.MM.yyyy for chart sorting
                                if (value instanceof Timestamp) {
                                    Timestamp ts = (Timestamp) value;
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                    return sdf.format(ts);
                                }
                                String text = value.toString();
                                // Try common patterns
                                DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                                String normalized = text;
                                try {
                                    DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    LocalDateTime ldt = LocalDateTime.parse(text, dtFmt);
                                    normalized = ldt.format(outFmt);
                                } catch (DateTimeParseException e1) {
                                    try {
                                        DateTimeFormatter dFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                        LocalDate ld = LocalDate.parse(text, dFmt);
                                        normalized = ld.format(outFmt);
                                    } catch (DateTimeParseException ignored) {
                                        // leave as-is
                                    }
                                }
                                return normalized;
                            }
                            return value.toString();
                        }
                ));
    }
}
