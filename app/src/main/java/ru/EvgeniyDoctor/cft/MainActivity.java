package ru.EvgeniyDoctor.cft;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


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
    //ArrayList <String> arrayList;
    ListView listView;
    String currencyOutputTemplate = "%s (%s)\nКурс: %s";
    AppPreferences pref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new AppPreferences(getApplicationContext());

        listView = findViewById(R.id.list);

        Helper.d("timestamp!");
        if (pref.contains(JSON.TIMESTAMP) && !pref.getString(JSON.TIMESTAMP, "").isEmpty()) {
            Helper.d(pref.getString(JSON.TIMESTAMP, ""));
        }

        Helper.d("currencies!");
        if (pref.contains(JSON.CURRENCIES) && !pref.getString(JSON.CURRENCIES, "").isEmpty()) {
            Helper.d(pref.getString(JSON.CURRENCIES, ""));
            String currencies = pref.getString(JSON.CURRENCIES, "");

            try {
                ArrayList<String> arrayList = getCurrenciesFromJson(new JSONObject(currencies));
                if (!arrayList.isEmpty()) {
                    setListView(arrayList);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    // loading new data
    public void getData(View view) {
        if (!Helper.checkInternetConnection(MainActivity.this)) {
            Toast.makeText(this, "Check your Internet connection", Toast.LENGTH_LONG).show();
            return;
        }
        new GetData().execute();
    }
    //-----------------------------------------------------------------------------------------------



    // getting info about all currencies from a JSON object and putting them in an array
    public ArrayList<String> getCurrenciesFromJson (JSONObject Valute) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            //arrayList.clear();
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



    // set currencies info to listView
    public void setListView (ArrayList<String> arrayList){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
    }
    //-----------------------------------------------------------------------------------------------



    //
    private class GetData extends AsyncTask <String, String, String> { // TODO
        ProgressDialog progressDialog;
        ArrayList <String> arrayList;



        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Wait…");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        //-----------------------------------------------------------------------------------------------



        @Override
        protected String doInBackground(String... strings) {
            JSON json = new JSON(getApplicationContext());

            String serverAnswer = json.getData(); // loading new data TODO null check
            String timestamp = json.getTimestamp(serverAnswer); // parse json to get timestamp
            JSONObject Valute = json.getCurrencies(serverAnswer); // parse json to get currencies

            arrayList = getCurrenciesFromJson(Valute); // array of strings for listView

            // save some info
            json.saveTimestamp(timestamp);
            json.saveCurrencies(Valute.toString());

            return null;
        }
        //-----------------------------------------------------------------------------------------------



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            if (!arrayList.isEmpty()) {
                setListView(arrayList);
            }
        }
        //-----------------------------------------------------------------------------------------------
    }
    // class
}




