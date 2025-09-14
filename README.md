# AssetsManagement

A desktop application built with Java 21 and JavaFX to record, track, and analyze personal assets with persistent storage in an Excel workbook using Apache POI.

## Features
- Record cumulative asset amounts by date: EUR, USD, 22K Gold (Bilezik), Ata Lira, Tam/Yarım/Çeyrek/Gram Altın
- Record market prices (Currencies page) by date
- Visualize unit-based growth over time with a line chart
- Total Overview chart for TRY totals (USD series can be toggled in code)
- Live total labels for latest values: Total ₺ and Total $
- Optional login screen (username/password: admin/admin)

## Tech Stack
- Java 21 (module system)
- JavaFX (controls, FXML)
- Apache POI (Excel read/write)
- Maven

## Project Structure
- `src/main/java/com/caslanqa/assetsmanagement/`
  - `AssetsManagementApplication` – main JavaFX app (loads `appPanel.fxml`)
  - `App` – alternate launcher that starts with `login.fxml`
  - `AssetsManagementsController` – main panel controller (charts, actions)
  - `LoginController` – login controller
- `src/main/java/com/caslanqa/utils/`
  - `FileUtils` – Excel IO (Assets & Currencies sheets)
  - `DateUtils` – date formatting utilities
  - `FormUtils` – simple map arithmetic helpers
- `src/main/resources/com/caslanqa/assetsmanagement/`
  - `appPanel.fxml`, `login.fxml`

## Data Storage
- Excel file path: `~/assets/assetsdb.xlsx`
- Sheets:
  - `Assets`: cumulative amounts per date (first row is headers)
  - `Currencies`: market prices per date (first row is headers)
- Column set is taken from the first insert; keep header names consistent.

## Prerequisites
- JDK 21+
- Maven 3.9+

## Run
Default run launches the main panel directly via the Maven JavaFX plugin.
```bash
mvn clean javafx:run
```

To start with the login window, run from your IDE using `com.caslanqa.assetsmanagement.App` as the main class, or change the plugin `mainClass` accordingly.

## How to Use
- Manage Asset(s) Record tab:
  - Enter date and amounts → click "Record Assets" to add to cumulative totals
  - Use "Delete Assets" with positive inputs to subtract from cumulative totals
- Currency Page:
  - Enter date and prices → click "Set Currencies" to save
  - Totals and charts refresh immediately
- Analysis Page:
  - Assets Unit Based: per-instrument growth over time
  - Total Overview ₺ $: TRY total series is shown; USD can be re-enabled in code

## Notes
- Date strings should be consistent (default `dd.MM.yyyy`).
- Totals are computed by matching the same-day price in `Currencies`; if not found, the nearest previous date (or last known) is used.
- Numeric parsing expects dot as decimal separator; non-numeric inputs are treated as zero.

## Configuration
- To change Excel file location, edit the `excelFile` path in `com.caslanqa.utils.FileUtils`.

## Troubleshooting
- Empty charts/totals: ensure both `Assets` and `Currencies` have at least one data row.
- File write errors: verify permissions to `~/assets`.
- Unexpected values: verify numeric fields only contain numbers.

## Build
```bash
mvn -q -DskipTests clean package
```

## License
Training/demo project. No explicit license.
