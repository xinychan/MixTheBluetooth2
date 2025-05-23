package com.test.assistant.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class FragmentButtonData {

    private final String mButtonNumbers = "ButtonNumber";
    private SharedPreferences mNameData, mButtonData, mButtonNumber;

    @SuppressLint("WrongConstant")
    public FragmentButtonData(Context context) {
        mNameData = context.getSharedPreferences("saveButtonName",
                MODE_APPEND | Context.MODE_PRIVATE);
        mButtonData = context.getSharedPreferences("saveButtonData",
                Context.MODE_PRIVATE | Context.MODE_APPEND);
        mButtonNumber = context.getSharedPreferences("saveButtonNumber",
                Context.MODE_PRIVATE | Context.MODE_APPEND);
    }

    public void saveButtonGroupNumber(int number) {
        SharedPreferences.Editor editor = mButtonNumber.edit();
        editor.putInt(mButtonNumbers, number);
        editor.apply();
    }

    public int getButtonGroupNumber() {
        return mButtonNumber.getInt(mButtonNumbers, 0);
    }

    public void saveButtonName(int viewId, String name, String data) {
        String key = String.valueOf(viewId);

        SharedPreferences.Editor editor = mNameData.edit();
        editor.putString(key, name);
        editor.apply();

        SharedPreferences.Editor editorData = mButtonData.edit();
        editorData.putString(key, data);
        editorData.apply();
    }

    public String getButtonName(int viewId) {
        String key = String.valueOf(viewId);
        return mNameData.getString(key, null);
    }

    public String getButtonData(int viewId) {
        String key = String.valueOf(viewId);
        return mButtonData.getString(key, null);
    }

    public void delete() {
        SharedPreferences.Editor editor = mNameData.edit();
        editor.clear();
        editor.apply();

        SharedPreferences.Editor editor1 = mButtonNumber.edit();
        editor1.clear();
        editor1.apply();

        SharedPreferences.Editor editor2 = mButtonData.edit();
        editor2.clear();
        editor2.apply();
    }

}
