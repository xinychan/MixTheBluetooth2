package com.test.assistant.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class WifiMessage {
    private SharedPreferences sharedPreferences;

    @SuppressLint("WrongConstant")
    public WifiMessage(Context context) {
        sharedPreferences = context.getSharedPreferences("data",
                MODE_APPEND | Context.MODE_PRIVATE);
    }

    public void saveData(String name, String pass) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, pass);
        editor.apply();
    }

    public String getWifiData(String name) {
        return sharedPreferences.getString(name, "");
    }

    public void deleteName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(name);
        editor.apply();
    }


}
