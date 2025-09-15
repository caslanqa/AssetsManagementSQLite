package com.caslanqa.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class CurrencyService {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * Gram altın, bilezik/22 ayar, tam altın, ata lira, yarım altın, çeyrek altın fiyatlarını döndürür.
     * @param apiKey CollectAPI API anahtarı
     * @return Map<String, Double> (anahtarlar: gramAltin, bilezik, tamAltin, ataLira, yarimAltin, ceyrekAltin)
     */
    public static Map<String, String> fetchGoldPrices(String apiKey) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.collectapi.com/economy/goldPrice"))
                .timeout(Duration.ofSeconds(10))
                .header("content-type", "application/json")
                .header("authorization", "apikey " + apiKey)
                .GET().build();

        HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        String responseBody = res.body();
        System.out.println("[goldPrice] status=" + res.statusCode());
        System.out.println("[goldPrice] body=" + responseBody);

        Map<String, String> goldPrices = new HashMap<>();
        // Varsayılan değerler
        goldPrices.put("gramAltin", "0.0");
        goldPrices.put("bilezik", "0.0");
        goldPrices.put("tamAltin", "0.0");
        goldPrices.put("ataLira", "0.0");
        goldPrices.put("yarimAltin", "0.0");
        goldPrices.put("ceyrekAltin", "0.0");

        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            JsonArray resultArr = json.getAsJsonArray("result");
            for (int i = 0; i < resultArr.size(); i++) {
                JsonObject obj = resultArr.get(i).getAsJsonObject();
                String name = obj.get("name").getAsString().toLowerCase();
                String price = obj.get("buying").getAsString();

                if (name.equalsIgnoreCase("gram altın")) {
                    goldPrices.put("gramAltin", price);
                } else if (name.equalsIgnoreCase("22 Ayar Bilezik")) {
                    goldPrices.put("bilezik", price);
                } else if (name.equalsIgnoreCase("tam altın")) {
                    goldPrices.put("tamAltin", price);
                } else if (name.equalsIgnoreCase("ata altın")) {
                    goldPrices.put("ataLira", price);
                } else if (name.equalsIgnoreCase("yarım altın")) {
                    goldPrices.put("yarimAltin", price);
                } else if (name.equalsIgnoreCase("çeyrek altın")) {
                    goldPrices.put("ceyrekAltin", price);
                }
            }
        } catch (Exception e) {
            // JSON parse hatası olursa, boş değerler döner
        }
        System.out.println("goldPrices = " + goldPrices);
        return goldPrices;
    }
    
    /**
     * TRY bazında dolar ve euro kurlarını döndürür.
     * @param apiKey CollectAPI API anahtarı
     * @return double[] {usdTry, eurTry}
     * @throws Exception
     */
    public static Map<String, String> fetchUsdEurTryRates(String apiKey) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.collectapi.com/economy/currencyToAll?int=10&base=TRY"))
                .timeout(Duration.ofSeconds(10))
                .header("content-type", "application/json")
                .header("authorization", "apikey " + apiKey)
                .GET().build();

        HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[currencyToAll] status=" + res.statusCode());
        System.out.println("[currencyToAll] body=" + res.body());

        String responseBody = res.body();

        String usdTry = "0.0";
        String eurTry = "0.0";
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            JsonArray resultArr = json.getAsJsonArray("result");
            for (int i = 0; i < resultArr.size(); i++) {
                JsonObject obj = resultArr.get(i).getAsJsonObject();
                String code = obj.get("code").getAsString();
                Double rate = obj.get("rate").getAsDouble();
                rate = 1 / rate;
                if ("USD".equalsIgnoreCase(code)) {
                    usdTry = rate.toString();
                } else if ("EUR".equalsIgnoreCase(code)) {
                    eurTry = rate.toString();
                }
            }
        } catch (Exception e) {
            // Fallback: try to parse manually if JSON parsing fails
            String usdKey = "\"code\":\"USD\"";
            String eurKey = "\"code\":\"EUR\"";
            int usdIdx = responseBody.indexOf(usdKey);
            int eurIdx = responseBody.indexOf(eurKey);
            if (usdIdx != -1) {
                int rateIdx = responseBody.indexOf("\"rate\":", usdIdx);
                if (rateIdx != -1) {
                    int commaIdx = responseBody.indexOf(",", rateIdx);
                    String rateStr = responseBody.substring(rateIdx + 7, commaIdx).replaceAll("[^0-9.]", "");
                    Double rateVal = Double.parseDouble(rateStr);
                    rateVal = 1 / rateVal;
                    usdTry = rateVal.toString();
                }
            }
            if (eurIdx != -1) {
                int rateIdx = responseBody.indexOf("\"rate\":", eurIdx);
                if (rateIdx != -1) {
                    int commaIdx = responseBody.indexOf(",", rateIdx);
                    String rateStr = responseBody.substring(rateIdx + 7, commaIdx).replaceAll("[^0-9.]", "");
                    Double rateVal = Double.parseDouble(rateStr);
                    rateVal = 1 / rateVal;
                    eurTry = rateVal.toString();
                }
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("dolar", usdTry);
        result.put("euro", eurTry);
        System.out.println("resultFiat = " + result);
        return result;
    }
}
