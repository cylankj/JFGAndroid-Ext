package com.cylan.ext.opt;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by cylan-hunt on 17-2-6.
 */

public class DebugOptionsImpl {
    private static final String TAG = "iDebugOptions";

    private DebugOptionsImpl() {
    }

    public static void enableCrashHandler(Context context, String dir) {
        Log.d("iDebugOptions", "enableCrashHandler");
        CrashHandler.getInstance().init(context, dir);
    }

    public static void enableStrictMode() {
        Log.d("iDebugOptions", "enableStrictMode");
        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder()).detectDiskReads().detectDiskWrites().detectNetwork().detectAll().penaltyLog().penaltyDialog().build());
    }

    public static String getServer() {
        // Open the file
        try {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "Smarthome" + File.separator + "log" + File.separator + "config.txt";
            FileInputStream fstream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine = "";
            String content = "";
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                content = strLine;
                Log.d("getServer", "getServer:" + strLine);
            }
            //Close the input stream
            br.close();
            return content;
        } catch (IOException e) {
            Log.e("IOException", ":" + e.getLocalizedMessage());
            return "";
        }
    }
}
