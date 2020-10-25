package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


public class App {
    final static private String API_KEY = System.getenv("BING_MAPS_KEY");
    final static private String HOST = "http://dev.virtualearth.net/REST/v1/Locations";

    public static void main(String[] args) {
        System.out.print("\n1 - адрес, 2 - координаты: ");
        Scanner scanner = new Scanner(System.in);
        String mode = scanner.nextLine();
        String result;


        if ("1".equals(mode)) {
            System.out.print("Введите адрес: ");
            result = request(scanner.nextLine(), GeoMethod.ADDRESS);
        } else if ("2".equals(mode)) {
            System.out.print("Введите координаты: ");
            result = request(scanner.nextLine(), GeoMethod.POINT);
        } else {
            result = "Выбранного режима не существует!";
        }

        System.out.println(result);
    }

    public static String request(String inputString, GeoMethod method) {
        String result = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) createURL(inputString, method).openConnection();

            //Конфигурирование запроса
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false);

            //Получаем ответ от сервера
            String response = null;
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                StringBuilder sb = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                connection.disconnect();
                response = sb.toString();
            } else {
                System.out.println("FIELD: " + connection.getResponseCode() + " CODE");
            }

            //Парсим json-ответ от сервера
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            JSONArray jsonArray = (JSONArray) jsonObject.get("resourceSets");
            jsonObject = (JSONObject) jsonArray.get(0);
            jsonArray = (JSONArray) jsonObject.get("resources");
            jsonObject = (JSONObject) jsonArray.get(0);

            if (method.equals(GeoMethod.ADDRESS)) {
                jsonObject = (JSONObject) jsonObject.get("point");
                jsonArray = (JSONArray) jsonObject.get("coordinates");

                result = jsonArray.get(0) + " " + jsonArray.get(1);
            } else {
                result = jsonObject.get("name").toString();
            }


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static URL createURL(String inputString, GeoMethod method) throws MalformedURLException {
        StringBuilder result = new StringBuilder(HOST);
        if (method == GeoMethod.ADDRESS) {
            result.append("?q=").append(inputString.replaceAll("\\s", "+")).append("&o=json&key=").append(API_KEY);
        } else {
            result.append("/").append(inputString.replaceAll("\\s", ",")).append("?o=json&key=").append(API_KEY);
        }

        return new URL(result.toString());
    }
}