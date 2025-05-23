package com.test.assistant.mqtt.expandableRecycler;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.assistant.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

/**
 * Created on 2019/5/15 4:52 PM
 */
public class HeaderViewHolder extends GroupViewHolder {
    private TextView genreName;
    private TextView hind;
    private TextView textView;
    private ImageView imageView;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        genreName = itemView.findViewById(R.id.list_item_genre_name);
        hind = itemView.findViewById(R.id.list_item_genre_hind);
        textView = itemView.findViewById(R.id.list_item_genre_text);
        imageView = itemView.findViewById(R.id.list_item_genre_icon);
    }


    @SuppressLint("SetTextI18n")
    public void setGenreTitle(ExpandableGroup genre) {
        if (genre instanceof Genre) {
            genreName.setText(genre.getTitle());
            textView.setText(((Genre) genre).getText());
            imageView.setImageResource(((Genre) genre).getIcon());
            if (((Genre) genre).getHind() != 0) {
                hind.setText("(" + ((Genre) genre).getHind() + ")");
            } else {
                hind.setText("");
            }
        }
    }


    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {

    }

    private void animateCollapse() {

    }
}
