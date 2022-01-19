package ru.EvgeniyDoctor.cft;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;



public class BackgroundUpdate extends Worker {
    public BackgroundUpdate(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    //-----------------------------------------------------------------------------------------------



    @NonNull
    @Override
    public Result doWork() {
        //Helper.f(getApplicationContext(), Helper.now() + ": doWork");

        JSON json = new JSON(getApplicationContext());
        String serverAnswer = json.getData(); // loading new data

        if (serverAnswer == null) { // error or nothing
            return null;
        }

        String timestamp = json.getTimestamp(serverAnswer); // parse json to get timestamp
        JSONObject Valute = json.getCurrencies(serverAnswer); // parse json to get currencies

        // save some info
        json.saveTimestamp(timestamp);
        json.saveCurrencies(Valute.toString());

        return null;
    }
    //-----------------------------------------------------------------------------------------------
}
