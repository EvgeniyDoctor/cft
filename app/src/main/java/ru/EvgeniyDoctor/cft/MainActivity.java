package ru.EvgeniyDoctor.cft;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
    ArrayList <String> arrayList;
    ListView listView;
    String currencyOutputTemplate = "%s (%s)\nКурс: %s";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayList = new ArrayList<>();
        listView = findViewById(R.id.list);
    }
    //-----------------------------------------------------------------------------------------------



    //
    public void getData(View view) {
        if (!Helper.checkInternetConnection(MainActivity.this)) {
            Toast.makeText(this, "Check your Internet connection", Toast.LENGTH_LONG).show();
            return;
        }
        new GetData().execute();
    }
    //-----------------------------------------------------------------------------------------------



    private class GetData extends AsyncTask <String, String, String> { // TODO
        ProgressDialog progressDialog;



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
            try {
                JSON json = new JSON();

                String serverAnswer = json.getData(); // TODO null check
                JSONObject Valute = json.getCurrencies(serverAnswer);

                //
                arrayList.clear();
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

            return null;
        }
        //-----------------------------------------------------------------------------------------------



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
            listView.setAdapter(adapter);
        }
        //-----------------------------------------------------------------------------------------------
    }
    // class
}




