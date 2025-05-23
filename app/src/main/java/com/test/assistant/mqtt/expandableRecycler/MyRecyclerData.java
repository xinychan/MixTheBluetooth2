package com.test.assistant.mqtt.expandableRecycler;

import com.test.assistant.R;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerData {

    private List<Genre> mList = new ArrayList<>();

    public List<Genre> getList() {
        return mList;
    }

    public boolean addData(String subtopic, String data) {

        for (Genre genre : mList) {
            if (genre.getTitle().equals(subtopic)) {
                genre.addItems(data);
                genre.addHint();
                return false;
            }
        }
        List<Artist> list = new ArrayList<>();
        list.add(new Artist(data, true));
        mList.add(0, new Genre(subtopic, list, R.drawable.ic_launcher_background));
        return true;
    }


}
