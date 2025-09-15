package com.caslanqa.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbHelper {
    private static final Logger LOGGER = Logger.getLogger(DbHelper.class.getName());
    public static String DB_URL;
    private static Integer currentUserId = null;
    public static String API_KEY = null;

    // --- CONFIG LOADING ---
    static {
        loadConfig();
        initializeDatabase();
    }

    private static void loadConfig() {
        try (InputStream input = new FileInputStream("src/main/resources/config.yml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(input);
            Map<String, Object> database = (Map<String, Object>) root.get("database");
            String dbPath = (String) database.get("path");

            // ${user.home} vb. placeholder'ları çöz
            dbPath = SystemSolver.resolvePlaceholders(dbPath);

            DB_URL = "jdbc:sqlite:" + dbPath;
            LOGGER.info("Database path: " + DB_URL);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Config.yml yüklenemedi, varsayılan path kullanılacak.", e);
            DB_URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/assets/assets.db";
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void setCurrentUserId(Integer userId) {
        currentUserId = userId;
    }

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static void clearCurrentUser() {
        currentUserId = null;
    }

    public static void initializeDatabase() {
        String createUserTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        api_key TEXT NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """;

        String createAssetsTable = """
                    CREATE TABLE IF NOT EXISTS asset_history (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                asset_type TEXT NOT NULL,
                                quantity REAL NOT NULL,
                                record_type TEXT NOT NULL CHECK (record_type IN ('+', '-')),
                                user_id INTEGER NOT NULL,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                            );
                
                """;

        String createCurrenciesTable = """
                    CREATE TABLE IF NOT EXISTS currencies (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        currency_code TEXT NOT NULL,
                        rate REAL NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """;

        String createNetAssetsTable = """
                    CREATE TABLE IF NOT EXISTS net_assets (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                euro REAL NOT NULL DEFAULT 0,
                                ataLira REAL NOT NULL DEFAULT 0,
                                tamAltin REAL NOT NULL DEFAULT 0,
                                gramAltin REAL NOT NULL DEFAULT 0,
                                ceyrekAltin REAL NOT NULL DEFAULT 0,
                                yarimAltin REAL NOT NULL DEFAULT 0,
                                bilezik REAL NOT NULL DEFAULT 0,
                                dolar REAL NOT NULL DEFAULT 0,
                                tl REAL NOT NULL DEFAULT 0,
                                user_id INTEGER NOT NULL,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                            );
                
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createUserTable);
            stmt.execute(createAssetsTable);
            stmt.execute(createCurrenciesTable);
            stmt.execute(createNetAssetsTable);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabanı tabloları oluşturulurken hata!", e);
        }
    }

    // --- USER REGISTER & LOGIN ---
    public static boolean registerUser(String username, String password, String apiKey) throws SQLException {
        if (isUsernameExists(username)) {
            throw new SQLException("Bu kullanıcı adı zaten kullanılıyor: " + username);
        }
        if (password == null || password.trim().length() < 4) {
            throw new SQLException("Şifre en az 4 karakter olmalıdır.");
        }

        String hashedPassword = BCrypt.hashpw(password.trim(), BCrypt.gensalt());

        Map<String, Object> values = new HashMap<>();
        values.put("username", username.trim());
        values.put("password", hashedPassword);
        values.put("api_key", apiKey);

        long userId = insert("users", values, false);
        if (userId > 0) {
            setCurrentUserId((int) userId);
            return true;
        }
        return false;
    }

    public static boolean authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT id, password FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(password, storedHash)) {
                        setCurrentUserId(rs.getInt("id"));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt("count") > 0;
            }
        }
    }

    public static long insert(String tableName, Map<String, Object> values, boolean addUserId) throws SQLException {
        if (addUserId && currentUserId != null) {
            values.putIfAbsent("user_id", currentUserId);
        }

        String columns = String.join(", ", values.keySet());
        String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            for (Object val : values.values()) {
                pstmt.setObject(i++, val);
            }
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }

    // --- GENERIC SELECT ---
    public static List<Map<String, Object>> select(String tableName, String whereClause,
                                                   boolean filterByUser, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        List<Object> allParams = new ArrayList<>(List.of(params));

        if (whereClause != null && !whereClause.isBlank()) {
            sql.append(" WHERE ").append(whereClause);
        }

        if (filterByUser && currentUserId != null) {
            sql.append(whereClause != null && !whereClause.isBlank() ? " AND " : " WHERE ").append("user_id = ?");
            allParams.add(currentUserId);
        }

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < allParams.size(); i++) {
                pstmt.setObject(i + 1, allParams.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        }
        return results;
    }

    public static long insertAssetForCurrentUser(String assetType, double quantity, String recordType) throws SQLException {
        if (currentUserId == null) {
            throw new SQLException("Kullanıcı oturumu açık değil!");
        }
        Map<String, Object> values = new HashMap<>();
        values.put("asset_type", assetType);
        values.put("quantity", quantity);
        values.put("record_type", recordType);
        values.put("user_id", currentUserId);

        return insert("asset_history", values, false);
    }

    public static long insertCurrencies(String currency_code, double rate) throws SQLException {
        if (currentUserId == null) {
            throw new SQLException("Kullanıcı oturumu açık değil!");
        }
        Map<String, Object> values = new HashMap<>();
        values.put("currency_code", currency_code);
        values.put("rate", rate);

        return insert("currencies", values, false);
    }

    public static List<Map<String, Object>> getAssetsForCurrentUser() throws SQLException {
        if (currentUserId == null) {
            throw new SQLException("Kullanıcı oturumu açık değil!");
        }
        return select("asset_history", null, true);
    }

    public static Map<String, Double> getLatestCurrencies() throws SQLException {
        Map<String, Double> latestRates = new HashMap<>();
        String sql = """
                    SELECT
                        t1.currency_code,
                        t1.rate
                    FROM
                        currencies t1
                    INNER JOIN
                        (SELECT currency_code, MAX(created_at) AS max_created_at FROM currencies WHERE rate > 0 GROUP BY currency_code) t2
                    ON
                        t1.currency_code = t2.currency_code AND t1.created_at = t2.max_created_at
                    WHERE
                        t1.rate > 0;
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String currencyCode = rs.getString("currency_code");
                double rate = rs.getDouble("rate");
                latestRates.put(currencyCode, rate);
            }
        }
        return latestRates;
    }

    public static List<Map<String, Object>> getSqlQuery(String sqlText) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sqlText)) {

            stmt.setInt(1, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    public static void setAPIKey() throws SQLException {
        String sql = "SELECT api_key FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    API_KEY = rs.getString("api_key");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}