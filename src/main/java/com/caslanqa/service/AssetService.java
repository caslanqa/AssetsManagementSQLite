package com.caslanqa.service;

import com.caslanqa.utils.DbHelper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> existing = new HashMap<>();

        netAssets.entrySet().forEach(keyValue ->
            existing.put(keyValue.getKey(), keyValue.getValue())
        );

        DbHelper.insert("net_assets", existing,true);
    }

    public static Map<String, Object> getLastNetAssets() throws SQLException {
        return DbHelper.getNetAssetsData(DbHelper.getCurrentUserId()).getLast();
    }
}
