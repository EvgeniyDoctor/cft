package ru.EvgeniyDoctor.cft;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Helper {
    static final String tag = "edoctor"; // tag for logs



    // checking if the network is available
    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo == null) ? false : true;

        /*
        if (netInfo == null) {
            return false;
        }
        return netInfo.isConnectedOrConnecting();
         */
    }
    //----------------------------------------------------------------------------------------------



    // make the first char Big
    public static String ucfirst (String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    //-----------------------------------------------------------------------------------------------



    //
    public static String now(){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return s.format(new Date());
    }
    //-----------------------------------------------------------------------------------------------



    // debug log
    public static <T> void d (T text) {
        try {
            Log.d(tag, text + "");
        }
        catch (Exception e) {
            Log.d(tag, "Helper d error:");
            e.printStackTrace();
        }
    }
    //-----------------------------------------------------------------------------------------------



    // log to file in private app folder
    public static <T> void f (Context context, T text) {
        File log = new File(
            new ContextWrapper(context).getDir("cft", Context.MODE_APPEND),
            "log.txt"
        );

        FileWriter writer = null;
        try {
            writer = new FileWriter(log, true);
            writer.write(text + "\n");
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            try {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }
    //-----------------------------------------------------------------------------------------------
}
