package com.test.assistant.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.os.ParcelFileDescriptor.MODE_APPEND;


public class DataMemory {

    private SharedPreferences sp, mSaveServiceMessage, mSaveServerList;

    private final String mServiceIp = "service";
    private final String mServicePort = "port";

    private final String mServersMessage = "ServersMessage";

    @SuppressLint("WrongConstant")
    public DataMemory(Context context) {
        sp = context.getSharedPreferences("data",
                MODE_APPEND | Context.MODE_PRIVATE);
        mSaveServiceMessage = context.getSharedPreferences("saveService",
                MODE_APPEND | Context.MODE_PRIVATE);
        mSaveServerList = context.getSharedPreferences("saveServerList",
                MODE_APPEND | Context.MODE_PRIVATE);
    }


    public void saveServicePort(String serviceIp, String port, String name) {
        Log.d("AppRun", "保存数据");
        SharedPreferences.Editor editor = mSaveServiceMessage.edit();
        editor.putString(mServiceIp, serviceIp);
        editor.putString(mServicePort, port);
        saveServerList(add(getSaveList(), serviceIp, port, name, getTime()));
        editor.apply();
    }

    public String getServicePort() {
        return mSaveServiceMessage.getString(mServicePort, null);
    }

    public String getServiceIp() {
        return mSaveServiceMessage.getString(mServiceIp, null);
    }

    public void deleteServicePort() {
        SharedPreferences.Editor editor = mSaveServiceMessage.edit();
        editor.clear();
        editor.apply();
    }


    public void saveData(String name, String pass) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(name, pass);
        editor.apply();
    }

    public String getData(String name) {
        return sp.getString(name, null);
    }


    public void delteData() {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }


    private void saveServerList(List<String> list) {
        Gson gson = new Gson();
        String data = gson.toJson(list);
        SharedPreferences.Editor editor = mSaveServerList.edit();
        editor.putString(mServersMessage, data);
        editor.apply();
    }

    private List<String> getSaveList() {
        String data = mSaveServerList.getString(mServersMessage, null);
        if (data == null)
            return null;
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    public Map<String, String> getServersMap() {
        List<String> list = getSaveList();
        if (list == null)
            return null;
        Map<String, String> map = new TreeMap<String, String>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        // 降序排序
                        return obj2.compareTo(obj1);
                    }
                });
        String key = "+&time*+";
        for (String s : list) {
            map.put(analysisKey(s), s.substring(0, s.indexOf(key)));
        }
        return map;
    }

    private List<String> add(List<String> list, String ip, String post, String name, String time) {
        if (list == null)
            list = new ArrayList<>();
        list.add(ip + ":" + post + "+&*+" + name + "+&time*+" + time);
        if (list.size() > 5) {
            list.remove(0);
        }
        return list;
    }

    private String getTime() {
        //获取系统的 日期
        Calendar calendar = Calendar.getInstance();

        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        String secondStr = second < 10 ? "0" + second : second + "";
        String minuteStr = minute < 10 ? "0" + minute : minute + "";
        String hourStr = hour < 10 ? "0" + hour : hour + "";
        String dayStr = day < 10 ? "0" + day : day + "";
        String monthStr = month < 10 ? "0" + month : month + "";

        return monthStr + "月" + dayStr + "日 " + hourStr + ":" + minuteStr + ":" + secondStr;
    }

    private String analysisKey(String data) {
        String key = "+&time*+";
        return data.substring(data.indexOf(key) + key.length());
    }

}
