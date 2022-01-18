package ru.EvgeniyDoctor.cft;

// some common things

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


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
}
