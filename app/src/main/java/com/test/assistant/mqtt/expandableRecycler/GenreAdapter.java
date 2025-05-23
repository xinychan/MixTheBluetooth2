package com.test.assistant.mqtt.expandableRecycler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.assistant.R;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;


/**
 * Created on 2019/5/15 5:00 PM
 */
public class GenreAdapter extends ExpandableRecyclerViewAdapter<HeaderViewHolder, ItemViewHolder> {

    private List<? extends ExpandableGroup> mList;


    public GenreAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
        mList = groups;
    }

    @Override
    public HeaderViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {//第一层
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_genre, parent, false);
        // Log.d("AppRun"+getClass().getSimpleName(),"onCreateGroupViewHolder");
        return new HeaderViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {//第二层
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_artist, parent, false);
        //Log.d("AppRun"+getClass().getSimpleName(),"onCreateChildViewHolder");
        return new ItemViewHolder(view);
    }

    //flatPosition: 总的列表长度
    //childIndex：子列表的长度
    @Override
    public void onBindChildViewHolder(ItemViewHolder holder, final int flatPosition,
                                      ExpandableGroup group, final int childIndex) {

        final Artist artist = ((Genre) group).getItems().get(childIndex);
        holder.setArtistName(artist.getName());
        holder.click(childIndex);
    }

    @Override
    public void onBindGroupViewHolder(HeaderViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        holder.setGenreTitle(group);
        //Log.d("AppRun"+getClass().getSimpleName(),"onBindGroupViewHolder");
    }


    //一级列表的点击事件
    @Override
    public boolean onGroupClick(int flatPos) {
        setGenre(flatPos);
        notifyDataSetChanged();
        return super.onGroupClick(flatPos);
    }


    private void setGenre(int flatPos) {
        int foldNumber = 0;
        int loop = 0;
        Log.d("AppRun" + getClass().getSimpleName(), "flatPos：" + flatPos);
        for (ExpandableGroup expandableGroup : mList) {
            if (loop + foldNumber < flatPos) {
                if (((Genre) expandableGroup).getUnfold()) {
                    foldNumber += ((Genre) expandableGroup).getItemNumber();
                }
                ++loop;
            } else {
                break;
            }
        }
        //Log.d("AppRun"+getClass().getSimpleName(),"想要点击： "+(flatPos-foldNumber) );
        ((Genre) mList.get(flatPos - foldNumber)).setHind().setUnfold();//设置为true
    }

}
