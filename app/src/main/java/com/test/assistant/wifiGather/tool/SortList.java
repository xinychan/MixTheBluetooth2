package com.test.assistant.wifiGather.tool;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xngly on 2019/9/10.
 * 排序
 */

public class SortList {

    public List<ScanResult> list;
    private List<ScanResult> temp;
    private String Name = "HC-25";


    public SortList(List<ScanResult> list) {
        this.list = new ArrayList<>();
        temp = new ArrayList<>();
        this.list = list;
    }

    public List<ScanResult> sorList() {
        int nameSize = Name.length();
        int listSize = list.size();
        boolean[] arr = new boolean[listSize];
        for (int i = 0; i < listSize; i++) {
            arr[i] = false;
        }

        for (int i = 0; i < listSize; i++) {
            if (cutString(list.get(i).SSID, nameSize) != null) {
                arr[i] = true;
                temp.add(list.get(i));
            }
        }

        for (int i = 0; i < listSize; i++) {
            if (!arr[i]) {
                temp.add(list.get(i));
            }
        }

        return temp;
    }

    private String cutString(String name, int nameSize) {
        String headName;
        if (name != null && name.length() >= nameSize) {
            headName = name.substring(0, nameSize);
            if (headName.equals(Name)) {
                return headName;
            } else {
                return null;
            }
        }
        return null;
    }

}
