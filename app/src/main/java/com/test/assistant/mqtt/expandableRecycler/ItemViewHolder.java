package com.test.assistant.mqtt.expandableRecycler;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.test.assistant.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created on 2019/5/15 4:59 PM
 */
public class ItemViewHolder extends ChildViewHolder {
    public TextView childTextView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        childTextView = itemView.findViewById(R.id.list_item_artist_name);
    }

    public void setArtistName(String name) {
        childTextView.setText(name);
    }

    /**
     * 子 item 的点击事件。
     *
     * @param position
     */
    public void click(final int position) {
        childTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: " + position);
            }
        });
    }
}

