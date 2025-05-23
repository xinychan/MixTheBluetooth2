package com.test.assistant.mqtt.expandableRecycler;


import com.test.assistant.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created on 2019/5/15 4:55 PM
 */
public class Genre extends ExpandableGroup<Artist> {
    private int iconResId;
    private String title;
    private int hind;
    private boolean isUnfold;//二级列表是否展开
    private int itemNumber;

    public Genre(String title, List<Artist> items, int iconResId) {
        super(title, items);
        this.iconResId = iconResId;
        this.title = title;
        hind = 1;
        itemNumber = 1;
        isUnfold = false;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void addItems(String data) {
        ++itemNumber;
        super.getItems().add(0, new Artist(data, true));
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public void setUnfold() {
        isUnfold = !isUnfold;
    }

    public boolean getUnfold() {
        return isUnfold;
    }

    public void addHint() {
        ++hind;
    }

    public int getHind() {
        return hind;
    }

    public Genre setHind() {
        this.hind = 0;
        return this;
    }

    public String getText() {
        return isUnfold ? "收起列表" : "展开列表";
    }

    public int getIcon() {
        return isUnfold ? R.mipmap.item_icon_bottom : R.mipmap.item_icon_right;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Genre)) {
            return false;
        }

        Genre genre = (Genre) o;

        return getIconResId() == genre.getIconResId();

    }

    @Override
    public int hashCode() {
        return getIconResId();
    }
}

