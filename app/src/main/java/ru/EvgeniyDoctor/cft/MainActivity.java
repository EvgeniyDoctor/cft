package ru.EvgeniyDoctor.cft;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;



/*
Описание задачи

Реализовать Android приложение со следующими функциями:

1. Загружать список валют с сайта ЦБ https://www.cbr-xml-daily.ru/daily_json.js и отображать его в виде списка
2. Предоставлять возможность конвертировать указанную сумму в рублях в выбранную валюту
3. Сохранять данные о курсах валют и не перезагружать их при повороте экрана или перезапуске приложения. Добавить возможность перезагрузить список курсов вручную.
4. Периодически обновлять курсы валют

Первая функция обязательна. Реализованные функции 2-4 будут увеличивать ваш шанс попасть на курс.
Приложение должно быть реализовано на языке java или kotlin.
Можно пользоваться любыми библиотеками, но необходимо объяснить в сопроводительном письме к заданию, почему каждая из библиотек была вами использована и почему именно эта, а не ее аналог.
Исходный код выполненного задания должен быть размещен в git репозитории (например, на github.com или bitbucket.org).

https://www.cbr-xml-daily.ru/daily_json.js
*/



public class MainActivity extends AppCompatActivity {
    ListView listView;
    TextView timestampView;
    TextView conversionResult;
    Spinner spinner;
    EditText editText;
    LinearLayout linearLayout;

    AppPreferences pref;
    String currencyOutputTemplate = "%s (%s)\nКурс: %s"; // template for listView
    String dateTimeOutputTemplate = "Актуальность данных: %s (МСК)";
    HashMap<String, String> codesAndRates; // hash, code : rate
    
    final String BUNDLE_CONVERT_RESULT = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new AppPreferences(getApplicationContext());

        listView            = findViewById(R.id.list);
        timestampView       = findViewById(R.id.timestampView);
        conversionResult    = findViewById(R.id.conversionResult);
        spinner             = findViewById(R.id.spinner);
        editText            = findViewById(R.id.editText);
        linearLayout        = findViewById(R.id.linearLayout);

        // timestamp loading
        if (pref.contains(JSON.TIMESTAMP)) {
            String timestamp = pref.getString(JSON.TIMESTAMP, "");
            setTimestampView(timestamp);
        }

        // currencies loading
        if (pref.contains(JSON.CURRENCIES)) {
            String currencies = pref.getString(JSON.CURRENCIES, "");
            try {
                setUI(getCurrenciesFromJson(new JSONObject(currencies)), currencies); // set items for listview, spinner; set visibility

                // load saved conversion result after changing orientation of the screen
                if (savedInstanceState != null) {
                    conversionResult.setText(savedInstanceState.getString(BUNDLE_CONVERT_RESULT, ""));
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    // set items for listview, spinner; set visibility
    public void setUI (ArrayList<String> formattedCurrencies, String currencies){
        if (formattedCurrencies.size() > 0) {
            setListView(formattedCurrencies);

            // spinner
            setHashCodesAndRates(currencies); // set hash
            setSpinner(); // set items to the spinner

            // show conversion block
            linearLayout.setVisibility(View.VISIBLE);
        }
    }
    //-----------------------------------------------------------------------------------------------



    // saving data before changing orientation of the screen
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_CONVERT_RESULT, conversionResult.getText().toString());
    }
    //-----------------------------------------------------------------------------------------------



    // set hash, code => rate
    public void setHashCodesAndRates (String currencies){
        try {
            codesAndRates = getCodesAndRatesFromJson(new JSONObject(currencies));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //-----------------------------------------------------------------------------------------------



    // set codes of the currencies in the spinner
    public void setSpinner(){
        ArrayList<String> codes = new ArrayList<>(codesAndRates.keySet());
        Collections.sort(codes);

        ArrayAdapter<String> adapterCodes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, codes);
        spinner.setAdapter(adapterCodes);
    }
    //-----------------------------------------------------------------------------------------------



    // conversion rubles to the selected currency
    public void convert (View view) {
        String text = editText.getText().toString();
        String selected = spinner.getSelectedItem().toString();

        if (text.isEmpty()) {
            Toast.makeText(this, "Введите количество рублей", Toast.LENGTH_SHORT).show();
            return;
        }

        double res = Integer.parseInt(editText.getText().toString()) * Double.parseDouble(codesAndRates.get(selected));
        conversionResult.setText(String.format("%.2f", res));
    }
    //-----------------------------------------------------------------------------------------------



    // parse timestamp from the json
    @SuppressLint("SimpleDateFormat")
    public String formatTimestamp (String timestamp) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date newDate = dateFormat.parse(timestamp);
            dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
            return dateFormat.format(newDate);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
    //-----------------------------------------------------------------------------------------------



    // loading new data
    public void getData(View view) {
        if (!Helper.checkInternetConnection(MainActivity.this)) {
            Toast.makeText(this, "Проверьте Интернет-соединение", Toast.LENGTH_LONG).show();
            return;
        }

        new GetData().execute();
        startBackgroundUpdate(); // start work manager
    }
    //-----------------------------------------------------------------------------------------------



    // start work manager
    private void startBackgroundUpdate(){
        PeriodicWorkRequest myWorkRequest =
            new PeriodicWorkRequest.Builder(BackgroundUpdate.class, 1, TimeUnit.HOURS)
                .setInitialDelay(10, TimeUnit.MINUTES)
                .addTag("workerTag")
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "workerTagOneAtATime",
            ExistingPeriodicWorkPolicy.KEEP,
            myWorkRequest
        );
        //WorkManager.getInstance(getApplicationContext()).enqueue(myWorkRequest);
    }
    //-----------------------------------------------------------------------------------------------



    // getting info about all currencies from a JSON object and putting them in an array
    public ArrayList<String> getCurrenciesFromJson (JSONObject Valute) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Iterator<String> temp = Valute.keys();
            while (temp.hasNext()) {
                String key = temp.next();
                JSONObject object = Valute.getJSONObject(key);

                arrayList.add(
                    String.format(
                        currencyOutputTemplate,
                        object.getString("Name"),
                        object.getString("CharCode"), object.getString("Value")
                    )
                );
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList;
    }
    //-----------------------------------------------------------------------------------------------



    // get hash like "code => rate"
    public HashMap<String, String> getCodesAndRatesFromJson(JSONObject Valute) {
        HashMap<String, String> hash = new HashMap<>();
        try {
            Iterator<String> temp = Valute.keys();
            while (temp.hasNext()) {
                String key = temp.next();
                JSONObject object = Valute.getJSONObject(key);

                hash.put(object.getString("CharCode"), object.getString("Value"));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return hash;
    }
    //-----------------------------------------------------------------------------------------------



    // set currencies info to listView
    public void setListView (ArrayList<String> arrayList){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
    }
    //-----------------------------------------------------------------------------------------------



    // set formatted timestamp to textView
    public void setTimestampView (String timestamp){
        String formattedTimestamp = formatTimestamp(timestamp);
        if (formattedTimestamp != null) {
            timestampView.setText(String.format(dateTimeOutputTemplate, formattedTimestamp));
        }
        else {
            timestampView.setText("Ошибка!");
        }
    }
    //-----------------------------------------------------------------------------------------------



    // CLASS
    private class GetData extends AsyncTask <Void, Void, Void> {
        private ProgressDialog progressDialog;
        private ArrayList <String> formattedCurrencies;
        String timestamp;
        String currencies;
        private boolean hasError;



        protected void onPreExecute() {
            super.onPreExecute();

            hasError = false;

            // screen orientation lock while ProgressDialog is showing, else will be "WindowLeaked" error
            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Подождите…");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        //-----------------------------------------------------------------------------------------------



        @Override
        protected Void doInBackground(Void... voids) {
            JSON json = new JSON(getApplicationContext());

            String serverAnswer = json.getData(); // loading new data

            if (serverAnswer == null) { // error or nothing
                hasError = true;
                return null;
            }

            timestamp = json.getTimestamp(serverAnswer); // parse json to get timestamp
            JSONObject Valute = json.getCurrencies(serverAnswer); // parse json to get currencies

            formattedCurrencies = getCurrenciesFromJson(Valute); // array of strings for listView
            currencies = Valute.toString();

            // save some info
            json.saveTimestamp(timestamp);
            json.saveCurrencies(currencies);

            return null;
        }
        //-----------------------------------------------------------------------------------------------



        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // unlock screen orientation

            if (hasError) {
                Toast.makeText(MainActivity.this, "Произошла ошибка!", Toast.LENGTH_LONG).show();
                return;
            }

            // set items for listview, spinner; set visibility
            setUI(formattedCurrencies, currencies);

            // timestamp
            if (!timestamp.isEmpty()) {
                setTimestampView(timestamp);
            }
        }
        //-----------------------------------------------------------------------------------------------



        // just in case
        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // unlock screen orientation
        }
    }
    // class
}




