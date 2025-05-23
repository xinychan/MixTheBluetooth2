package com.test.assistant.recyclerAdapter;

import android.content.Context;

import com.test.assistant.R;
import com.test.assistant.commonAdapter.ItemClickListener;
import com.test.assistant.commonAdapter.RecyclerCommonAdapter;
import com.test.assistant.commonAdapter.ViewHolder;

import java.util.List;

/**
 * Created by xngly on 2019/4/3.
 */

public class FragmentListAdapter extends RecyclerCommonAdapter<Resou> {
    public FragmentListAdapter(Context context, List<Resou> strings, int layoutId) {
        super(context, strings, layoutId);
    }

    @Override
    protected void convert(ViewHolder holder, Resou item, int position, ItemClickListener itemClickListener) {
        if (item.getState() != 0) {
            holder.setText(R.id.fragment_item_name, item.getmStr())
                    .setText(R.id.fragment_item_ip, item.getmIP())
                    .setViewState(R.id.fragment_item_switch, false)
                    .setViewState(R.id.fragment_item_loading, false)
                    .setViewState(R.id.fragment_item_view, false)
                    .setViewState(R.id.fragment_item_state, true)
                    .setTextAndColor(R.id.fragment_item_state, item.getStateText(),
                            item.getStateTextColor());
            if (item.getState() == 2) {
                holder.setViewState(R.id.fragment_item_loading, true)
                        .setViewState(R.id.fragment_item_view, true);
            }
            return;
        }
        holder.setText(R.id.fragment_item_name, item.getmStr())
                .setText(R.id.fragment_item_ip, item.getmIP())
                .setText(R.id.fragment_item_show_ip, item.getServersIP())
                .setText(R.id.fragment_item_show_post, item.getServersPost())
                .setViewState(R.id.fragment_item_state, false)
                .setImageResource(R.id.fragment_item_switch, item.getClick())
                .setLinearLayout(R.id.fragment_item_hint, item.getHintLinearLayout())
                .setOnclickListener(R.id.fragment_item_hide, position, itemClickListener)
                .setOnclickListener(R.id.fragment_item_unfold, position, itemClickListener)
                .setOnclickListener(R.id.fragment_item_fill, position, itemClickListener)
                .setOnclickListener(R.id.fragment_item_message, position, itemClickListener);
    }

}
