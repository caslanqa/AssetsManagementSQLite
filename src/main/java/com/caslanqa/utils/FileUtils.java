package com.caslanqa.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class FileUtils {

    private static final String assetsSheet = "Assets";
    private static final String currenciesSheet = "Currencies";
    private static final File excelFile = new File(System.getProperty("user.home") + "/assets/assetsdb.xlsx");


    public static void recordAssets(Map<String, String> data) {
        try {
            Workbook workbook = getOrCreateWorkbook();
            Sheet sheet = getOrCreateSheet(workbook, assetsSheet);

            if (isNewFile(sheet)) {
                System.out.println("data.keySet() = " + data.keySet());
                createHeaderRow(sheet, data.keySet());
            }

            Row headerRow = sheet.getRow(0);
            Row newRow = createNewDataRow(sheet, data, headerRow);

            saveWorkbook(workbook);
            System.out.println("Yeni kümülatif satır eklendi -> " + excelFile.getAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException("Excel'e yazarken hata oluştu: " + e.getMessage(), e);
        }
    }

    public static void deleteAssets(Map<String, String> data) {
        try {
            Workbook workbook = getOrCreateWorkbook();
            Sheet sheet = getOrCreateSheet(workbook,assetsSheet);

            if (isNewFile(sheet)) {
                throw new RuntimeException("Excel dosyası boş, silinecek veri yok.");
            }

            Row headerRow = sheet.getRow(0);
            Row newRow = createNewDataRow(sheet, data, headerRow);

            saveWorkbook(workbook);
            System.out.println("Yeni kümülatif satır eklendi -> " + excelFile.getAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException("Excel'e yazarken hata oluştu: " + e.getMessage(), e);
        }
    }

    public static void recordCurrencies(Map<String, String> data) {
        try {
            Workbook workbook = getOrCreateWorkbook();
            Sheet sheet = getOrCreateSheet(workbook, currenciesSheet);

            if (isNewFile(sheet)) {
                createHeaderRow(sheet, data.keySet());
            }

            Row headerRow = sheet.getRow(0);
            Row newRow = createNewCurrencyRow(sheet, data, headerRow);

            saveWorkbook(workbook);
            System.out.println("Yeni döviz satırı eklendi -> " + excelFile.getAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException("Excel'e yazarken hata oluştu: " + e.getMessage(), e);
        }
    }

    private static Workbook getOrCreateWorkbook() throws IOException {
        ensureParentDirectoryExists();

        if (excelFile.exists()) {
            try (FileInputStream fis = new FileInputStream(excelFile)) {
                return new XSSFWorkbook(fis);
            }
        } else {
            return new XSSFWorkbook();
        }
    }

    private static void ensureParentDirectoryExists() {
        File parentDir = excelFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    private static boolean isNewFile(Sheet sheet) {
        return sheet.getPhysicalNumberOfRows() == 0;
    }

    private static void createHeaderRow(Sheet sheet, Set<String> headers) {
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;
        for (String key : headers) {
            headerRow.createCell(cellIndex++).setCellValue(key);
        }
    }

    private static Row createNewDataRow(Sheet sheet, Map<String, String> data, Row headerRow) {
        int lastRowNum = sheet.getLastRowNum();
        Row previousRow = getPreviousRow(sheet, lastRowNum);
        Row newRow = sheet.createRow(lastRowNum + 1);

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String headerName = getHeaderName(headerRow, i);
            String newValue = data.get(headerName);

            Cell newCell = newRow.createCell(i);
            setCellValue(newCell, newValue, previousRow, i);
        }

        return newRow;
    }

    private static Row createNewCurrencyRow(Sheet sheet, Map<String, String> data, Row headerRow) {
        int lastRowNum = sheet.getLastRowNum();
        Row previousRow = getPreviousRow(sheet, lastRowNum);
        Row newRow = sheet.createRow(lastRowNum + 1);

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String headerName = getHeaderName(headerRow, i);
            String newValue = data.get(headerName);

            Cell newCell = newRow.createCell(i);
            setCellValue(newCell, newValue, previousRow, i);
        }

        return newRow;
    }

    private static Row getPreviousRow(Sheet sheet, int lastRowNum) {
        return lastRowNum > 0 ? sheet.getRow(lastRowNum) : null;
    }

    private static String getHeaderName(Row headerRow, int cellIndex) {
        Cell headerCell = headerRow.getCell(cellIndex);
        return headerCell != null ? headerCell.getStringCellValue() : "";
    }

    private static void setCellValue(Cell newCell, String newValue, Row previousRow, int columnIndex) {
        if(previousRow == null) {
            setCellValueWithNewData(newCell, newValue, previousRow, columnIndex);
        } else if (!newValue.equals("0") && previousRow != null) {
            setCellValueWithNewData(newCell, newValue, previousRow, columnIndex);
        } else {
            setCellValueFromPreviousData(newCell, previousRow, columnIndex);
        }
    }

    private static void setCellValueWithNewData(Cell newCell, String newValue, Row previousRow, int columnIndex) {
        try {
            double numericValue = Double.parseDouble(newValue);
            double cumulativeValue = getCumulativeValue(previousRow, columnIndex);
            newCell.setCellValue(cumulativeValue + numericValue);
        } catch (NumberFormatException e) {
            newCell.setCellValue(newValue);
        }
    }

    private static void setCellValueFromPreviousData(Cell newCell, Row previousRow, int columnIndex) {
        if (previousRow != null) {
            Cell prevCell = previousRow.getCell(columnIndex);
            if (prevCell != null) {
                copyCellValue(prevCell, newCell);
            }
        }
    }

    private static double getCumulativeValue(Row previousRow, int columnIndex) {
        if (previousRow != null) {
            Cell prevCell = previousRow.getCell(columnIndex);
            return getNumericValueFromCell(prevCell);
        }
        return 0.0;
    }

    private static double getNumericValueFromCell(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0.0;
    }

    private static void copyCellValue(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case NUMERIC:
                targetCell.setCellValue(sourceCell.getNumericCellValue());
                break;
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            default:
                targetCell.setCellValue("");
        }
    }

    private static void saveWorkbook(Workbook workbook) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(excelFile)) {
            workbook.write(fos);
        } finally {
            workbook.close();
        }
    }

    private static Sheet getOrCreateSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        return sheet;
    }

    public static List<Map<String, String>> readAssetsData() {
        List<Map<String, String>> dataList = new ArrayList<>();

        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = workbook.getSheet(assetsSheet);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                return dataList;
            }

            Row headerRow = sheet.getRow(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Map<String, String> rowData = new HashMap<>();
                    for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                        String header = headerRow.getCell(j).getStringCellValue();
                        Cell cell = row.getCell(j);
                        String value = cell != null ? getCellValueAsString(cell) : "0";
                        rowData.put(header, value);
                    }
                    dataList.add(rowData);
                }
            }
        } catch (Exception e) {
            System.err.println("Excel okuma hatası: " + e.getMessage());
        }

        return dataList;
    }

    public static List<Map<String, String>> readCurrenciesData() {
        List<Map<String, String>> dataList = new ArrayList<>();

        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = workbook.getSheet(currenciesSheet);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                return dataList;
            }

            Row headerRow = sheet.getRow(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Map<String, String> rowData = new HashMap<>();
                    for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                        String header = headerRow.getCell(j).getStringCellValue();
                        Cell cell = row.getCell(j);
                        String value = cell != null ? getCellValueAsString(cell) : "0";
                        rowData.put(header, value);
                    }
                    dataList.add(rowData);
                }
            }
        } catch (Exception e) {
            System.err.println("Excel okuma hatası (Currencies): " + e.getMessage());
        }

        return dataList;
    }

    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

}
