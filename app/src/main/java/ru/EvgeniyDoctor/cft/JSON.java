package ru.EvgeniyDoctor.cft;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class JSON {
    AppPreferences pref;
    final static String TIMESTAMP = "timestamp";
    final static String CURRENCIES = "currencies";



    JSON (Context context) {
        pref = new AppPreferences(context);
    }
    //-----------------------------------------------------------------------------------------------



    // load new json data
    public String getData(){
        try {
            URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // connect to the server
            try {
                if (urlConnection.getResponseCode() == 200) { // restful code 200 (OK)
                    Helper.d("Connect OK");
                    urlConnection.connect();
                }
            }
            catch (IOException e) {
                Helper.d("HTTP answer != OK");
                //e.printStackTrace();
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
    //-----------------------------------------------------------------------------------------------



    // get currencies from the json object
    public JSONObject getCurrencies (String serverAnswer){
        try {
            JSONObject dataJsonObj = new JSONObject(serverAnswer);
            JSONObject Valute = dataJsonObj.getJSONObject("Valute");
            return Valute;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    //-----------------------------------------------------------------------------------------------



    //
    public String getTimestamp (String serverAnswer){
        try {
            JSONObject dataJsonObj = new JSONObject(serverAnswer);
            return dataJsonObj.getString("Timestamp");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    //-----------------------------------------------------------------------------------------------



    //
    public void saveTimestamp (String value){
        pref.put(TIMESTAMP, value);
    }
    //-----------------------------------------------------------------------------------------------



    //
    public void saveCurrencies (String value){
        pref.put(CURRENCIES, value);
    }
    //-----------------------------------------------------------------------------------------------
}




































