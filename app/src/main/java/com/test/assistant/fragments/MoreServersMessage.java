package com.test.assistant.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.test.assistant.R;
import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.storage.DataMemory;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.OnLongClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

import java.util.Map;


public class MoreServersMessage extends LinearLayout {

    @ViewById(R.id.more_record)
    private LinearLayout mRecord;

    @ViewById(R.id.more_record_content)
    private TextView mRecordContent;

    @ViewById(R.id.fragment_more_first)
    private TextView mFirstTv;

    @ViewById(R.id.fragment_more_second)
    private TextView mSecondTv;

    @ViewById(R.id.fragment_more_thirdly)
    private TextView mThirdlyTv;

    @ViewById(R.id.fragment_more_fourthly)
    private TextView mFourthlyTv;

    @ViewById(R.id.fragment_more_fifth)
    private TextView mFifthTv;

    private DataMemory mDataMemory;

    private TextView mServersIP, mServersPost;

    private CommonDialog.Builder builder;

    public MoreServersMessage(Context context) {
        this(context, null);
    }

    public MoreServersMessage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoreServersMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.fragment_more_menu, this);
        ViewUtils.inject(this);
        initAll();
    }

    private void initAll() {
        initData();
    }

    private void initData() {
        mDataMemory = new DataMemory(getContext());
        clearTextView();
        Map<String, String> map = mDataMemory.getServersMap();
        for (String key : map.keySet()) {
            setTextView(analysis(map.get(key), key));
        }
    }

    @OnClick({R.id.fragment_more_first, R.id.fragment_more_second, R.id.fragment_more_thirdly,
            R.id.fragment_more_fourthly, R.id.fragment_more_fifth})
    private void clickListener(View view) {
        String message = ((TextView) view).getText().toString();
        mServersIP.setText(message.substring(0, message.indexOf(":")));
        mServersPost.setText(message.substring(message.indexOf(":") + 1, message.indexOf("\n")));
        builder.dismiss();
    }

    @OnLongClick({R.id.fragment_more_first, R.id.fragment_more_second, R.id.fragment_more_thirdly,
            R.id.fragment_more_fourthly, R.id.fragment_more_fifth})
    private void longClickListener(View view) {
        String start = "+&*+";
        String message = ((TextView) view).getText().toString();
        Map<String, String> map = mDataMemory.getServersMap();
        String data = map.get(message.substring(message.indexOf("\n") + "\n".length()));
        mRecord.setVisibility(VISIBLE);
        mRecordContent.setText(data.substring(data.indexOf(start) + start.length()));
    }

    @OnClick(R.id.more_back_list)
    private void back() {
        mRecord.setVisibility(GONE);
    }

    private void setTextView(String data) {
        if (mFirstTv.getText().toString().isEmpty()) {
            mFirstTv.setText(data);
            mFirstTv.setVisibility(VISIBLE);
        } else if (mSecondTv.getText().toString().isEmpty()) {
            mSecondTv.setText(data);
            mSecondTv.setVisibility(VISIBLE);
        } else if (mThirdlyTv.getText().toString().isEmpty()) {
            mThirdlyTv.setText(data);
            mThirdlyTv.setVisibility(VISIBLE);
        } else if (mFourthlyTv.getText().toString().isEmpty()) {
            mFourthlyTv.setText(data);
            mFourthlyTv.setVisibility(VISIBLE);
        } else if (mFifthTv.getText().toString().isEmpty()) {
            mFifthTv.setText(data);
            mFifthTv.setVisibility(VISIBLE);
        }
    }

    private void clearTextView() {
        mFirstTv.setText("");
        mSecondTv.setText("");
        mThirdlyTv.setText("");
        mFourthlyTv.setText("");
        mFifthTv.setText("");
    }


    public void setServersMessage(TextView ip, TextView post) {
        this.mServersIP = ip;
        this.mServersPost = post;
    }

    public MoreServersMessage setBuilder(CommonDialog.Builder builder) {
        this.builder = builder;
        return this;
    }

    private String analysis(String data, String time) {
        String key = "+&*+";
        String v = data.substring(0, data.indexOf(key));
        return v + "\n" + time;
    }

}
